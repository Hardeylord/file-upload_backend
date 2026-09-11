package com.merging.chunks.repo;

import com.merging.chunks.enums.STATUS;
import com.merging.chunks.model.Uploads;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UploadsRepo extends JpaRepository<Uploads, String> {
    Uploads findByFileNameAndFileSize(String filename, BigInteger filesize);

    @Modifying
    @Transactional
    @Query("DELETE FROM Uploads u WHERE u.uploadId= :uploadId")
    void deleteByUploadId(@Param("uploadId") String uploadId);

    Optional<Uploads> findByUploadId(String uploadId);
    @Modifying
    @Transactional
    @Query("UPDATE Uploads u SET u.status= 'COMPLETING' WHERE u.uploadId = :uploadId AND u.status != 'COMPLETING' ")
    int markCompleting(@Param("uploadId") String uploadId);

    @Query("SELECT u FROM Uploads u WHERE u.uploadId = :uploadId AND u.status = 'COMPLETED' ")
    Optional<Uploads> findByStatus(@Param("uploadId") String uploadId);

    @Modifying
    @Transactional
    @Query("UPDATE Uploads u SET u.status= 'FAILED' WHERE u.uploadId = :uploadId")
    void marFailed(@Param("uploadId") String uploadId);

//    @Modifying
//    @Transactional
//    @Query("Select ALL FROM Uploads u WHERE u.status='PROCESSING'")
//    List<Uploads> getByStatus();

    List<Uploads> getAllByStatus(STATUS processing);

//    @Query("SELECT u FROM Uploads u WHERE u.user.id =:userId")
    @Query(value = "SELECT * FROM uploads WHERE user_id = :userId", nativeQuery = true)
    List<Uploads> findMyUploads(@Param("userId") UUID userId);
}
