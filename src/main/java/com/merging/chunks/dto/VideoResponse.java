package com.merging.chunks.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoResponse {
    private String id;
    private String title;
    private String description;
    private String stramUrl;
    private double duration;
    private List<String> resolutions;
}
