package com.merging.chunks.controller;

import com.merging.chunks.dto.VideoCardDTO;
import com.merging.chunks.dto.VideoResponse;
import com.merging.chunks.model.Video;
import com.merging.chunks.service.VideoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/video")
    public ResponseEntity<List<VideoCardDTO>> searchVideo (@RequestParam("search") String search) {
        return ResponseEntity.ok(videoService.videoSimilaritySearch(search));
    }

    @GetMapping("/categories")
    public ResponseEntity<?> categories () {
        return ResponseEntity.ok("OK");
    }
}
