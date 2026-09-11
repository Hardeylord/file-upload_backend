package com.merging.chunks.repo;

import com.merging.chunks.dto.VideoCardDTO;
import com.merging.chunks.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

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

    @Modifying
    @Query("SELECT v FROM Video v WHERE v.ownerId=:user_id")
    List<Video> findVideosByUserId(@Param("user_id") UUID user_id);
}
