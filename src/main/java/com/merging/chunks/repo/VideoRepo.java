package com.merging.chunks.repo;

import com.merging.chunks.dto.VideoCardDTO;
import com.merging.chunks.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VideoRepo extends JpaRepository<Video, String> {

    @Query("""
            SELECT new com.merging.chunks.dto.VideoCardDTO(
                        v.id,
                        v.title,
                        v.thumbnail,
                        v.duration
                    )
                    FROM Video v
            """)
    List<VideoCardDTO> getAllVideos();
}
