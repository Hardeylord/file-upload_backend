package com.merging.chunks.service;

import com.merging.chunks.dto.TranscodeJob;
import com.merging.chunks.enums.JOB;
import com.merging.chunks.enums.STATUS;
import com.merging.chunks.model.JobRecord;
import com.merging.chunks.model.Uploads;
import com.merging.chunks.model.Video;
import com.merging.chunks.repo.ChunksRepo;
import com.merging.chunks.repo.UploadsRepo;
import com.merging.chunks.repo.VideoRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@Slf4j
public class RequestVideoTranscodeService {
    private final UploadsRepo uploadsRepo;
    private final VideoRepo videoRepo;
    private final String bucket = System.getenv("AWS_BUCKET");
    private final TranscodingQueue transcodingQueue;
    private final JobRegistry jobRegistry;
    private final VectorStore vectorStore;
    private final ThreadPoolExecutor threadPoolExecutor;
    public RequestVideoTranscodeService(UploadsRepo uploadsRepo, VideoRepo videoRepo, TranscodingQueue transcodingQueue, JobRegistry jobRegistry, VectorStore vectorStore, ThreadPoolExecutor threadPoolExecutor) {
        this.uploadsRepo = uploadsRepo;
        this.videoRepo = videoRepo;
        this.transcodingQueue = transcodingQueue;
        this.jobRegistry = jobRegistry;
        this.vectorStore = vectorStore;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    public ResponseEntity<ReqVideoTrc> requestTranscode(String uploadId, String key, String title, String description, List<String> categories) {
        Optional<Uploads> uploads = uploadsRepo.findByStatus(uploadId);
        if (uploads.isEmpty()) {
            log.warn("VIDEO STILL PROCESSING {}", uploadId);
            var response = new ReqVideoTrc("Upload still processing", true, "wait for video upload to complete");
            return ResponseEntity.status(HttpStatusCode.valueOf(422)).body(response);
        }
        UUID videoId= UUID.randomUUID();

        threadPoolExecutor.submit(()->
                GenerateEmbeddings(title, description, categories, String.valueOf(videoId)));
//            CREATE VIDEO ROW
        var VIDEO = Video.builder()
                .id(videoId.toString())
                .filename(uploads.get().getFileName())
                .title(title)
                .description(description)
                .categories(categories)
                .uploads(uploads.get())
                .status(STATUS.QUEUED.name())
                .sizeMB(uploads.get().getFileSize())
                .ownerId(uploads.get().getUsers().getId())
                .build();
        videoRepo.save(VIDEO);
        var job = new TranscodeJob(videoId.toString(), JOB.TRANSCODE_VIDEO.name(), uploadId, key);
        jobRegistry.createRedisJob(createJob(job, videoId));
        transcodingQueue.enqueue(job);
        var resp = new ReqVideoTrc("video now processing", false, "You will get an email for when your video is streamable");
        return ResponseEntity.ok(resp);
    }

    public void GenerateEmbeddings(String title, String description, List<String> categories, String id) {
        String content = """
                Title:%s
                Description:%s
                Categories:%s\s
               \s""".formatted(title, description, String.join(", ",categories));
        Document document = new Document(content,
                Map.of(
                        "video_id",id,
                        "video_title",title,
                        "video_categories",categories
                ));
        TextSplitter tokenTextSplitter = TokenTextSplitter.builder().build();
        vectorStore.accept(tokenTextSplitter.apply(List.of(document)));

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

    public record ReqVideoTrc (
           String message,
           Boolean error,
           String fix
    ) {

    }
}
