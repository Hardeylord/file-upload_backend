package com.merging.chunks.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadIdsDTO {
    private String key;
    private String uploadId;
}
