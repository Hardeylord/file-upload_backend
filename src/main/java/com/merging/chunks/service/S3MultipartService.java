package com.merging.chunks.service;

import com.merging.chunks.dto.UploadIdsDTO;
import com.merging.chunks.enums.ChunkStatus;
import com.merging.chunks.enums.STATUS;
import com.merging.chunks.model.Chunks;
import com.merging.chunks.model.Uploads;
import com.merging.chunks.repo.ChunksRepo;
import com.merging.chunks.repo.UploadsRepo;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.*;

import java.io.IOException;
import java.math.BigInteger;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class S3MultipartService {

    @Value("${vne.s3Config.bucket.name}")
    private String bucket;

    String filePath= "etub/";

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final ChunksRepo chunksRepo;
    private final UploadsRepo uploadsRepo;
    private final ApplicationEventPublisher publisher;

    public S3MultipartService(S3Client s3Client, S3Presigner s3Presigner, ChunksRepo chunksRepo, UploadsRepo uploadsRepo, ApplicationEventPublisher publisher) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.chunksRepo = chunksRepo;
        this.uploadsRepo = uploadsRepo;
        this.publisher = publisher;
    }


//    Generate Upload Id
    public UploadIdsDTO getS3UploadId(String fileName, BigInteger fileSize) {

        CreateMultipartUploadResponse multiUploadResp = s3Client.createMultipartUpload(
                CreateMultipartUploadRequest.builder()
                        .key(fileName)
                        .bucket(bucket)
                        .build()
        );
        String uid = multiUploadResp.uploadId();
        UploadIdsDTO ids = new UploadIdsDTO(fileName, uid);

//        ids.setUploadId(uid);
//        ids.setKey(multiUploadResp.key());

        Uploads fileUpload = new Uploads();
        fileUpload.setUploadId(uid);
        fileUpload.setStatus(STATUS.PROCESSING);
        fileUpload.setFileName(fileName);
        fileUpload.setFileSize(fileSize);

        uploadsRepo.save(fileUpload);
        return ids;
    }

//    Returns Uploaded Chunk based on filename
    public ResponseEntity<List<Integer>> testChunk(String filename, BigInteger filesize) {
       Uploads uploads = uploadsRepo.findByFileNameAndFileSize(filename, filesize);
        String id = uploads.getUploadId();
        System.out.println("founded " + id);
        ListPartsResponse uploadedChunks = s3Client.listParts(ListPartsRequest.builder()
                        .bucket(bucket)
                        .key(filename)
                        .uploadId(id)
                        .build());
        List<Integer> parts = uploadedChunks.parts().stream().map(Part::partNumber).toList();


            return ResponseEntity.ok().body(parts);
    }

//    chunk -> server -> S3 upload
    public ResponseEntity<?> multipartUpload(MultipartFile file, String fileName, Integer partNumber, String uploadId, int totalChunksNumber) throws IOException {

        UploadPartResponse uploadPartResponse = s3Client.uploadPart(UploadPartRequest.builder()
                     .partNumber(partNumber)
                     .bucket(bucket)
                     .uploadId(uploadId)
                     .key(fileName)
                     .build(), RequestBody.fromBytes(file.getBytes()));
        String etag = uploadPartResponse.eTag().replace("\"", "");

        Chunks completedChunks = new Chunks();

        completedChunks.setFilename(fileName);
        completedChunks.setUploadId(uploadId);
        completedChunks.setEtag(etag);
        completedChunks.setPartNumber(partNumber);
        completedChunks.setStatus(ChunkStatus.UPLOADED);

        publisher.publishEvent(completedChunks);

        if (Objects.equals(partNumber, totalChunksNumber)) {
            try{
                completeMultipart(uploadId, fileName);
                return ResponseEntity.ok("FILE MERGED");
            } catch (Exception e) {
                System.out.println(e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return ResponseEntity.ok("UPLOADED");
    }

//    Complete upload and call S3 to merge
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

//    Cancel upload and delete already uploaded chunks
    public ResponseEntity<?> abortMultipartUpload(String uploadId, String filename) {

        System.out.println("Attempted");
        chunksRepo.deleteAllByUploadId(uploadId);
        uploadsRepo.deleteByUploadId(uploadId);

        try {
            ListPartsResponse uploadedChunks = s3Client.listParts(ListPartsRequest.builder()
                    .bucket(bucket)
                    .key(filename)
                    .uploadId(uploadId)
                    .build());

            uploadedChunks.parts().forEach(p->{
                System.out.println(p+" Already uploaded");
            });

            if (!uploadedChunks.parts().isEmpty()) abortS3Upload(uploadId, filename);
            return ResponseEntity.ok().build();
        } catch (S3Exception e) {
            System.out.println("s3err -> "+e.getMessage());
            throw new RuntimeException(e);
        }
    }

//    method that calls S3 to abort upload
    public void abortS3Upload(String uploadId, String filename) {
        AbortMultipartUploadResponse abortUpload = s3Client.abortMultipartUpload(
                AbortMultipartUploadRequest.builder()
                        .bucket(bucket)
                        .uploadId(uploadId)
                        .key(filename)
                        .build());
    }

//    Saves metadata , checks if all part has been uploaded and merges
    @Transactional
    public void completePresignedUpload(String filename, Integer partnumber, String uploadId, String etag, Integer totalPartNumber) {

        Chunks completedChunks = new Chunks();

        completedChunks.setFilename(filename);
        completedChunks.setUploadId(uploadId);
        completedChunks.setEtag(etag);
        completedChunks.setPartNumber(partnumber);
        completedChunks.setStatus(ChunkStatus.UPLOADED);

        chunksRepo.save(completedChunks);

        long count = chunksRepo.countByUploadId(uploadId);

        if (count < totalPartNumber) return;
        publisher.publishEvent(new UploadIdsDTO(filename, uploadId));
    }

//    Generate Presigned URL
    public String getChunksPresignedUrl(String filename, Integer partnumber, String uploadId) {

        try {
            UploadPartRequest uploadPartRequest =
                    UploadPartRequest.builder()
                            .bucket(bucket)
                            .key(filename)
                            .uploadId(uploadId)
                            .partNumber(partnumber)
                            .build();

            UploadPartPresignRequest presignRequest =
                    UploadPartPresignRequest.builder()
                            .signatureDuration(Duration.ofMinutes(10))
                            .uploadPartRequest(uploadPartRequest)
                            .build();

            PresignedUploadPartRequest presignedRequest =
                    s3Presigner.presignUploadPart(presignRequest);

            return presignedRequest.url().toString();
        } catch (S3Exception e) {
            throw new RuntimeException(e);
        }

    }

//    List Object In bucket V2
    public ResponseEntity<?> listObject(String folder) {

        if (!Objects.equals(folder, "null")) {
            try {
                ListObjectsV2Response objectsV2Response = s3Client.listObjectsV2(
                        ListObjectsV2Request.builder()
                                .bucket(bucket)
                                .prefix(folder)
                                .build()
                );
                List<String> files = objectsV2Response.contents()
                        .stream().map(S3Object::key).toList();

                return ResponseEntity.ok().body(files);
            } catch (S3Exception e) {
                System.out.println("UNABLE TO FETCH FILES"+ " : " +e.getMessage());
                throw new RuntimeException(e);
            }
        }

        try {
            ListObjectsV2Response objectsV2Response = s3Client.listObjectsV2(
                    ListObjectsV2Request.builder()
                            .bucket(bucket)
                            .build()
            );
            List<String> fileList = objectsV2Response.contents()
                    .stream().map(S3Object::key).toList();
            return ResponseEntity.ok(fileList);
        } catch (S3Exception e) {
            System.out.println("UNABLE TO FETCH FILES"+ " : " +e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
