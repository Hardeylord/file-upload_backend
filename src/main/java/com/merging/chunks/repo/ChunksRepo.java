package com.merging.chunks.repo;

import com.merging.chunks.model.Chunks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ChunksRepo extends JpaRepository<Chunks, String> {
    List<Chunks> findByUploadIdOrderByPartNumberAsc(String uploadId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Chunks c WHERE c.uploadId = :uploadId")
    void deleteAllByUploadId(@Param("uploadId") String uploadId);


    @Query("SELECT COUNT(*) FROM Chunks c WHERE c.uploadId = :uploadId")
    long countByUploadId(@Param("uploadId") String uploadId);


}
