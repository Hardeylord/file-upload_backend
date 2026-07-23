package com.merging.chunks.repo;

import com.merging.chunks.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VideoRepo extends JpaRepository<Video, String> {

}
