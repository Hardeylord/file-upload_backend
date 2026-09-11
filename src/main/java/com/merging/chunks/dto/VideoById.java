package com.merging.chunks.dto;


import com.merging.chunks.model.Uploads;
import com.merging.chunks.model.Video;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class VideoById {
    private List<Video> videoList;
    private boolean error;
    private String message;
}
