package com.merging.chunks.service;

import com.merging.chunks.dto.VideoCardDTO;
import com.merging.chunks.dto.VideoResponse;
import com.merging.chunks.model.Video;
import com.merging.chunks.repo.VideoRepo;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class VideoService {

    private final String CLOUD_FRONT_URL = System.getenv("AWS_CLOUDFRONT_URL");

    private final VideoRepo videoRepo;
    private final VectorStore vectorStore;

    public VideoService(VideoRepo videoRepo, VectorStore vectorStore) {
        this.videoRepo = videoRepo;
        this.vectorStore = vectorStore;
    }

    public ResponseEntity<List<VideoCardDTO>> getAllVideos () {
        List<Video> videos = videoRepo.findAll();
        List<VideoCardDTO> allVideos = videoRepo.getAllVideos();
        return ResponseEntity.ok(allVideos);
    }

    public VideoResponse videoStream(String id) {
        Video video = videoRepo.findById(id).orElseThrow(()-> new RuntimeException("NO SUCH VIDEO"));
        return new VideoResponse(
                video.getId(),
                video.getTitle(),
                video.getDescription(),
                video.getCategories(),
                CLOUD_FRONT_URL + video.getMasterplaylist(),
                video.getDuration(),
                video.getResolutions()
        );
    }

    public List<VideoCardDTO> videoSimilaritySearch(String searchRequest) {
        List<Document> relevantChunks = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .topK(4)
                        .query(searchRequest)
                        .build()
        );

        if (relevantChunks.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> searchResultId = relevantChunks.stream().map(doc->
            doc.getMetadata().get("video_id").toString()
        ).distinct().toList();

        return videoRepo.findAllById(searchResultId).stream()
                .filter(Objects::nonNull)
                .map(video-> new VideoCardDTO(
                 video.getId(),
                 video.getTitle(),
                 video.getThumbnail(),
                 video.getDuration())).toList();
    }
}
