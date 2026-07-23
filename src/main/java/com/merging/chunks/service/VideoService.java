package com.merging.chunks.service;

import com.merging.chunks.dto.VideoResponse;
import com.merging.chunks.model.Video;
import com.merging.chunks.repo.VideoRepo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VideoService {
    @Value("${vne.s3Config.base.url}")
    private String baseUrl;
    @Value("${vne.s3Config.CLOUDFRONT.url}")
    private String CLOUD_FRONT_URL;

    private final VideoRepo videoRepo;

    public VideoService(VideoRepo videoRepo) {
        this.videoRepo = videoRepo;
    }

    public ResponseEntity<List<Video>> getAllVideos () {
        List<Video> videos = videoRepo.findAll();

        return ResponseEntity.ok(videos);
    }

    public VideoResponse videoStream(String id) {
        Video video = videoRepo.findById(id).orElseThrow(()-> new RuntimeException("NO SUCH VIDEO"));
        return new VideoResponse(
                video.getId(),
                video.getTitle(),
                video.getDescription(),
                CLOUD_FRONT_URL + video.getMasterplaylist(),
                video.getDuration(),
                video.getResolutions()
        );
    }
}
