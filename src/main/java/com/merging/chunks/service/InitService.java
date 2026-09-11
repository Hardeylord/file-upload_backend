package com.merging.chunks.service;

import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Map;

@Service
public class InitService {

    private final RedisTemplate<String, Object> redisTemplate;

    public InitService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void createStreamGroup() {
        if (!redisTemplate.hasKey("transcoding_stream")) {
            System.out.println("Streams Not found");
            redisTemplate.opsForStream().add(
                    StreamRecords.newRecord()
                            .ofMap(Map.of("init", "true"))
                            .withStreamKey("transcoding_stream")
            );
        }
        try {
            System.out.println("Attempting to Create");
            redisTemplate.opsForStream().createGroup(
                    "transcoding_stream",
                    ReadOffset.from("0-0"),
                    "transcoding_group");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
