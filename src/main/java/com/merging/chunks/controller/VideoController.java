package com.merging.chunks.controller;

import com.merging.chunks.dto.VideoById;
import com.merging.chunks.dto.VideoCardDTO;
import com.merging.chunks.dto.VideoResponse;
import com.merging.chunks.service.UploadService;
import com.merging.chunks.service.VideoService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class VideoController {
    private final VideoService videoService;
    private final UploadService uploadService;
    public VideoController(VideoService videoService, UploadService uploadService) {
        this.videoService = videoService;
        this.uploadService = uploadService;
    }

    @GetMapping("/videos")
    public ResponseEntity<List<VideoCardDTO>> videos () {
        return videoService.getAllVideos();
    }

    @GetMapping("/video/{id}")
    public ResponseEntity<VideoResponse> video (@PathVariable String id) {
        return ResponseEntity.ok(videoService.videoStream(id));
    }

    @GetMapping("/my-videos")
    public ResponseEntity<VideoById> getVideoByUserId() {
        return uploadService.findMyVideos();
    }

    @PostMapping("/edit-video")
    public ResponseEntity<String> editVideo(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("categories") List<String> categories,
            @RequestParam("videoId") String videoId,
            @RequestParam("key") String key,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
            HttpServletRequest request
            ) {
        System.out.println("VIDEO ID "+videoId);
        if (!thumbnail.isEmpty()) {
            System.out.println("USING FILE");
        return ResponseEntity.ok(uploadService.saveVideo(title, description, categories, videoId, thumbnail, key));
        }
        String thumbnailUrl = request.getParameter("thumbnail");
        System.out.println("USING URL -> "+thumbnailUrl);
        return ResponseEntity.ok(uploadService.saveVideo(title, description, categories, videoId, thumbnailUrl));
    }

    @GetMapping("/search")
    public ResponseEntity<List<VideoCardDTO>> searchVideo (@RequestParam("q") String search) {
        return ResponseEntity.ok(videoService.videoSimilaritySearch(search));
    }

    @GetMapping("/categories")
    public ResponseEntity<?> categories () {
        return ResponseEntity.ok("OK");
    }
}
