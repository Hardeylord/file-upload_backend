package com.merging.chunks.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class TranscodeJob {
    private String videoId;
    private String type;
    private String uploadId;
    private String input;
}
