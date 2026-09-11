package com.merging.chunks.service;

import com.merging.chunks.dto.MyUserDetails;
import com.merging.chunks.dto.UploadDTO;
import com.merging.chunks.dto.VideoById;
import com.merging.chunks.model.Uploads;
import com.merging.chunks.model.Users;
import com.merging.chunks.model.Video;
import com.merging.chunks.repo.UploadsRepo;
import com.merging.chunks.repo.UsersRepo;
import com.merging.chunks.repo.VideoRepo;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.*;

@Service
public class UploadService {

    private final String bucket = System.getenv("AWS_BUCKET");
    private final String CLOUD_FRONT_URL = System.getenv("AWS_CLOUDFRONT_URL");

    private final UploadsRepo uploadsRepo;
    private final UsersRepo usersRepo;
    private final VideoRepo videoRepo;
    private final S3Client s3Client;
    public UploadService(UploadsRepo uploadsRepo, UsersRepo usersRepo, VideoRepo videoRepo, S3Client s3Client) {
        this.uploadsRepo = uploadsRepo;
        this.usersRepo = usersRepo;
        this.videoRepo = videoRepo;
        this.s3Client = s3Client;
    }

    public ResponseEntity<UploadDTO> getAllUploads() {
        var securityContext = (MyUserDetails) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        assert securityContext != null;
        Optional<Users> user = findUser(securityContext.getId());
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new UploadDTO(new ArrayList<>(),true, "USER NOT FOUND, CREATE ACCOUNT OR LOGIN TO CONTINUE"));
        }
        List<Uploads> myUploads = uploadsRepo.findMyUploads(user.get().getId());
        return ResponseEntity.status(HttpStatus.OK).body(new UploadDTO(myUploads,false, ""));
    }

    public ResponseEntity<VideoById> findMyVideos () {
        var securityContext = (MyUserDetails) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
        assert securityContext != null;
        Optional<Users> user = findUser(securityContext.getId());
        if (user.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new VideoById(new ArrayList<>(),true, "USER NOT FOUND, CREATE ACCOUNT OR LOGIN TO CONTINUE"));
        }
        List<Video> videoById = videoRepo.findVideosByUserId(securityContext.getId());
        return ResponseEntity.status(HttpStatus.OK).body(new VideoById(videoById,false, ""));
    }

    public String saveVideo(String title, String description, List<String> categories,
                            String videoId, String thumbnailUrl) {
        Optional<Video> videoById = videoRepo.findById(videoId);
        if (videoById.isEmpty()) {
            return "ERROR... No Such video";
        }
        Video video = videoById.get();
        video.setTitle(title);
        video.setDescription(description);
        video.setCategories(categories);
        video.setThumbnail(thumbnailUrl);
        videoRepo.save(video);
        return "Successfully saved changes.. Refresh";
    }

    public String saveVideo(String title, String description, List<String> categories,
                            String videoId, MultipartFile thumbnail, String key) {
        Optional<Video> videoById = videoRepo.findById(videoId);
        if (videoById.isEmpty()) {
            return "ERROR... No Such video";
        }

        Video video = videoById.get();
        video.setTitle(title);
        video.setDescription(description);
        video.setCategories(categories);
        video.setThumbnail(CLOUD_FRONT_URL+key+thumbnail.getOriginalFilename());
        videoRepo.save(video);
        return uploadThumbnail(key, thumbnail);
    }

    private String uploadThumbnail(String key, MultipartFile thumbnail) {
        try {
            PutObjectResponse putObjectResponse = s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key+thumbnail.getOriginalFilename())
                            .build(),
                    RequestBody.fromBytes(thumbnail.getBytes())
            );
            return "Successfully uploaded to s3";
        } catch (S3Exception | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Optional<Users> findUser(UUID id) {
        return usersRepo.findById(id);
    }
}
