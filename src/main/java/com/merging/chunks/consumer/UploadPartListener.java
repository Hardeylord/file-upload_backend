package com.merging.chunks.consumer;

import com.merging.chunks.dto.UploadIdsDTO;
import com.merging.chunks.enums.STATUS;
import com.merging.chunks.model.Chunks;
import com.merging.chunks.model.Uploads;
import com.merging.chunks.repo.ChunksRepo;
import com.merging.chunks.repo.UploadsRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CompleteMultipartUploadRequest;
import software.amazon.awssdk.services.s3.model.CompletedMultipartUpload;
import software.amazon.awssdk.services.s3.model.CompletedPart;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.util.List;

@Component
public class UploadPartListener {
    private final ChunksRepo chunksRepo;
    private final S3Client s3Client;
    private final UploadsRepo uploadsRepo;
    @Value("${vne.s3Config.bucket.name}")
    private String bucket;

    public UploadPartListener(ChunksRepo chunksRepo, S3Client s3Client, UploadsRepo uploadsRepo) {
        this.chunksRepo = chunksRepo;
        this.s3Client = s3Client;
        this.uploadsRepo = uploadsRepo;
    }

    @Async
    @EventListener
    @TransactionalEventListener
    public void handlePartUpload(UploadIdsDTO dto) {
        int updated = uploadsRepo.markCompleting(dto.getUploadId());
        if (updated == 0) return;
        try{
            completeMultipart(dto.getUploadId(), dto.getKey());
            uploadsRepo.markCompleted(dto.getUploadId());
        } catch (S3Exception e) {
            uploadsRepo.marFailed(dto.getUploadId());
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
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
