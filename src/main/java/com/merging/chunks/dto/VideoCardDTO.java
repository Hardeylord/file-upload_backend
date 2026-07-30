package com.merging.chunks.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoCardDTO {
    private String id;
    private String title;
    private String thumbnail;
    private double duration;
}
