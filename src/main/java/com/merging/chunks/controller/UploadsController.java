package com.merging.chunks.controller;

import com.merging.chunks.dto.UploadDTO;
import com.merging.chunks.service.RequestVideoTranscodeService;
import com.merging.chunks.service.UploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class UploadsController {
    private final UploadService uploadService;
    private final RequestVideoTranscodeService transcodeService;

    public UploadsController(UploadService uploadService, RequestVideoTranscodeService transcodeService) {
        this.uploadService = uploadService;
        this.transcodeService = transcodeService;
    }

    @GetMapping("uploads")
    public ResponseEntity<UploadDTO> getMyUploads() {
       return uploadService.getAllUploads();
    }

    @PostMapping("request-streaming")
    public ResponseEntity<RequestVideoTranscodeService.ReqVideoTrc> requestStreamingService(@RequestParam("uploadId") String uploadId,
                                                                                            @RequestParam("title") String title,
                                                                                            @RequestParam("description") String description,
                                                                                            @RequestParam("key") String key,
                                                                                            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
                                                                                            @RequestParam("categories") List<String> categories) {

        return transcodeService.requestTranscode(uploadId, key, title, description,  categories);
    }
}
