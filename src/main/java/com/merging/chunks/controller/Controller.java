package com.merging.chunks.controller;

import com.merging.chunks.dto.PresignedDTO;
import com.merging.chunks.dto.UploadIdsDTO;
import com.merging.chunks.service.DirService;
import com.merging.chunks.service.S3MultipartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigInteger;

@RestController
public class Controller {
    private final DirService dirService;
    private final S3MultipartService s3MultipartService;
    private final String UPLOAD_DIRECTORY = "C:\\Users\\Administrator\\Desktop\\ServletJsp\\chunks\\src\\main\\resources\\uploads";

    public Controller(DirService dirService, S3MultipartService s3MultipartService) {
        this.dirService = dirService;
        this.s3MultipartService = s3MultipartService;
    }

//    @GetMapping("/upload")
//    public ResponseEntity<?> checkChunk(@RequestParam Integer resumableChunkNumber, @RequestParam String resumableIdentifier, @RequestParam String resumableFilename) {
//        Path folder = Paths.get(UPLOAD_DIRECTORY,resumableIdentifier, "chunk-"+resumableChunkNumber);
//
//        boolean folderExist = Files.exists(folder);
//
//        if (folderExist) {
//            System.out.println("UPLOADED");
//            return ResponseEntity.ok("FILE FULLY UPLOADED");
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }

    @PostMapping("/upload")
    public ResponseEntity<?> getChunk(@RequestParam("file") MultipartFile file,
                         @RequestParam String resumableIdentifier,
                         @RequestParam Integer resumableChunkNumber,
                         @RequestParam Integer resumableTotalChunks,
                         @RequestParam String resumableFilename) throws IOException {
       String resolved = dirService.uploadToDircectory(file, resumableFilename, resumableIdentifier, resumableChunkNumber, resumableTotalChunks);
        return ResponseEntity.ok("Saved to "+resolved);
    }


//    generate upload-ID
    @GetMapping("/CreateMultipartUpload")
    public ResponseEntity<UploadIdsDTO> createUploadId(@RequestParam("filename") String filename,
                                                       @RequestParam("filesize") BigInteger filesize) {

          UploadIdsDTO idsDTO = s3MultipartService.getS3UploadId(filename, filesize);
        System.out.println(idsDTO.getUploadId());
        return ResponseEntity.ok().body(idsDTO);
    }

//    Chunk saving by server
    @PostMapping("/multipartUpload")
    public ResponseEntity<?> chunkUpload(@RequestParam("file") MultipartFile file,
                                         @RequestParam("filename") String filename,
                                         @RequestParam("partnumber") int partnumber,
                                         @RequestParam("uploadid") String uploadId,
                                         @RequestParam("totalparts") int totalparts) throws IOException {

       return s3MultipartService.multipartUpload(file,filename,partnumber,uploadId, totalparts);
    }

//    GET already uploaded chunk list
    @GetMapping("/testChunk")
    public ResponseEntity<?> checkUploadedChunk(@RequestParam("filename") String filename,
                                                @RequestParam("filesize") BigInteger filesize) {
       return s3MultipartService.testChunk(filename, filesize);
    }

//    Cancel upload and remove from S3 Bucket
    @PostMapping("/cancelUpload")
    public ResponseEntity<?> abortMultipartUpload(@RequestParam("filename") String filename,
                                                  @RequestParam("uploadId") String uploadId) {
       return s3MultipartService.abortMultipartUpload(uploadId, filename);
    }

//    Save uploaded metadata with eTag from client
    @PostMapping("/presignedUploadChunks")
    public ResponseEntity<?> presignedUploadedChunks(@RequestParam("etag") String etag,
                                                     @RequestParam("filename") String filename,
                                                     @RequestParam("partnumber") int partnumber,
                                                     @RequestParam("uploadid") String uploadId,
                                                     @RequestParam("totalparts") int totalparts) {
        s3MultipartService
                .completePresignedUpload(filename, partnumber, uploadId, etag, totalparts);
        return ResponseEntity.ok("OK");
    }

//   Generate Presigned URL
    @GetMapping("/upload/generatePresignedUrl")
    public ResponseEntity<String> getPresignedUrl(@RequestParam("partnumber") Integer partnumber,
                                             @RequestParam("uploadId") String uploadId,
                                             @RequestParam("filename") String filename) {

        String presignedUrl = s3MultipartService
                .getChunksPresignedUrl(filename, partnumber, uploadId);

        PresignedDTO pdto = new PresignedDTO();
        pdto.setUrl(presignedUrl);
        pdto.setStatusResponse("UPLOADED");

        return ResponseEntity.ok().body(presignedUrl);
    }

    @GetMapping("/files")
    public ResponseEntity<?> getFiles(@RequestParam(required = false) String folder) {
        System.out.println("FOLDER -> "+folder);
       return s3MultipartService.listObject(folder);
    }

    @GetMapping("unfinishedUpload")
    public ResponseEntity<?> getUnfinishedFileUpload() {
        return s3MultipartService.getUnfinishedFileUpload();
    }
}




