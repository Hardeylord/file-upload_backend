package com.merging.chunks.controller;

import com.merging.chunks.dto.UploadDTO;
import com.merging.chunks.service.UploadService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class UploadsController {
    private final UploadService uploadService;

    public UploadsController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @GetMapping("uploads")
    public ResponseEntity<UploadDTO> getMyUploads() {
       return uploadService.getAllUploads();
    }
}
