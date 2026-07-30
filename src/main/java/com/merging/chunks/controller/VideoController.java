package com.merging.chunks.controller;

import com.merging.chunks.dto.VideoCardDTO;
import com.merging.chunks.dto.VideoResponse;
import com.merging.chunks.model.Video;
import com.merging.chunks.service.VideoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class VideoController {
    private final VideoService videoService;

    public VideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping("/videos")
    public ResponseEntity<List<VideoCardDTO>> videos () {
        return videoService.getAllVideos();
    }

    @GetMapping("/video/{id}")
    public ResponseEntity<VideoResponse> video (@PathVariable String id) {
        return ResponseEntity.ok(videoService.videoStream(id));
    }

    @GetMapping("/categories")
    public ResponseEntity<?> categories () {
        return ResponseEntity.ok("OK");
    }
}
