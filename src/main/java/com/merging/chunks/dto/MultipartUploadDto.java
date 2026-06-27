package com.merging.chunks.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class MultipartUploadDto {
    private int partNumber;
    private String uploadId;
    private MultipartFile file;
    private String filename;
}
