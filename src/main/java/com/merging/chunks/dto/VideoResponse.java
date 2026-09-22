package com.merging.chunks.dto;

import com.merging.chunks.model.VideoTranscript;
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
    private List<String> categories;
    private String stramUrl;
    private double duration;
    private List<String> resolutions;
    private List<VideoTranscript> videoTranscripts;
}
