package com.merging.chunks.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;
import java.util.List;

@Data
public class CompletedChunks {
    private String fileName;
    private List<Integer> completedPart;
    private BigInteger fileSize;
    private String uploadId;
}
