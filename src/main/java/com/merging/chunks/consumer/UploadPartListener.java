package com.merging.chunks.consumer;

import com.merging.chunks.dto.TranscodeJob;
import com.merging.chunks.dto.UploadIdsDTO;
import com.merging.chunks.enums.JOB;
import com.merging.chunks.enums.STATUS;
import com.merging.chunks.model.Chunks;
import com.merging.chunks.model.JobRecord;
import com.merging.chunks.model.Uploads;
import com.merging.chunks.model.Video;
import com.merging.chunks.repo.ChunksRepo;
import com.merging.chunks.repo.UploadsRepo;
import com.merging.chunks.repo.VideoRepo;
import com.merging.chunks.service.JobRegistry;
import com.merging.chunks.service.TranscodingQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
public class UploadPartListener {
    private final ChunksRepo chunksRepo;
    private final S3Client s3Client;
    private final UploadsRepo uploadsRepo;
    private final VideoRepo videoRepo;
    private final RedisTemplate<String, Object> redisTemplate;
    private final String bucket = System.getenv("AWS_BUCKET");
    private final TranscodingQueue transcodingQueue;
    private final JobRegistry jobRegistry;

    public UploadPartListener(ChunksRepo chunksRepo, S3Client s3Client, UploadsRepo uploadsRepo, VideoRepo videoRepo, RedisTemplate redisTemplate, TranscodingQueue transcodingQueue, JobRegistry jobRegistry) {
        this.chunksRepo = chunksRepo;
        this.s3Client = s3Client;
        this.uploadsRepo = uploadsRepo;
        this.videoRepo = videoRepo;
        this.redisTemplate = redisTemplate;
        this.transcodingQueue = transcodingQueue;
        this.jobRegistry = jobRegistry;
    }

    @Async
    @EventListener
    @TransactionalEventListener
    public void handlePartUpload(UploadIdsDTO dto) {
        int updated = uploadsRepo.markCompleting(dto.getUploadId());
        if (updated == 0) return;
        try{
            String uploadId=dto.getUploadId();
            String key=dto.getKey();
            completeMultipart(uploadId, key);
//            MARKS QUEUED [READY FOR QUEUE] ****METHOD NAME IS WEIRD ....
            Optional<Uploads> uploads = uploadsRepo.findByStatus(uploadId);
            if (uploads.isEmpty()) {
                log.warn("VIDEO STILL PROCESSING {}", dto.getUploadId());
            return;
            }
//            Uploads uploads = uploadsRepo.findByUploadId(uploadId).orElseThrow(() -> new RuntimeException("NO SUCH UPLOAD"));
            UUID videoId= UUID.randomUUID();
//            CREATE VIDEO ROW
            var vid = Video.builder()
                    .id(videoId.toString())
                    .filename(uploads.get().getFileName())
                    .uploads(uploads.get())
                    .status(STATUS.QUEUED.name())
                    .sizeMB(uploads.get().getFileSize())
                    .ownerId(uploads.get().getUsers().getId())
                    .build();
            videoRepo.save(vid);
            var job = new TranscodeJob(videoId.toString(), JOB.TRANSCODE_VIDEO.name(), uploadId, key);
            jobRegistry.createRedisJob(createJob(job, videoId));
            transcodingQueue.enqueue(job);
        } catch (S3Exception e) {
            uploadsRepo.marFailed(dto.getUploadId());
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private JobRecord createJob(TranscodeJob job, UUID videoId) {
        return new JobRecord(
                videoId,
                JOB.TRANSCODE_VIDEO.name(),
                STATUS.QUEUED.name(),
                0,
                3,
                job.getInput(),
                null,
                null,
                Instant.now(),
                Instant.now()
        );
    }

    public void completeMultipart(String uploadId, String fileName) {

        List<Chunks> completedChunks = chunksRepo.findByUploadIdOrderByPartNumberAsc(uploadId);
        List<CompletedPart> completedParts = completedChunks.stream().
                map(b->CompletedPart.builder()
                        .eTag(b.getEtag())
                        .partNumber(b.getPartNumber())
                        .build())
                .toList();

        s3Client.completeMultipartUpload(CompleteMultipartUploadRequest.builder()
                .uploadId(uploadId)
                .key(fileName)
                .bucket(bucket)
                .multipartUpload(CompletedMultipartUpload.builder().parts(completedParts).build())
                .build());

        Uploads completedUpload = uploadsRepo.findByUploadId(uploadId).orElseThrow(()-> new RuntimeException("FILE NOT FOUND"));
        completedUpload.setStatus(STATUS.COMPLETED);
        uploadsRepo.save(completedUpload);
        System.out.println("COMPLETED");
    }
}
