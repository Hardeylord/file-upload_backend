package com.merging.chunks.repo;

import com.merging.chunks.model.Video;
import com.merging.chunks.model.VideoTranscript;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VideoTranscriptRepo extends JpaRepository<VideoTranscript, UUID> {
    @Query("SELECT vt FROM VideoTranscript vt WHERE vt.video.id= :videoId ORDER BY vt.start ASC")
    List<VideoTranscript> findAllByVideo(@Param("videoId") String videoId);
}
