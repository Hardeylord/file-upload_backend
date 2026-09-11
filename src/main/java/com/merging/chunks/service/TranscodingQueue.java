package com.merging.chunks.service;

import com.merging.chunks.dto.TranscodeJob;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ObjectRecord;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.hash.JacksonHashMapper;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
public class TranscodingQueue {
    private final RedisTemplate<String , Object> redisTemplate;

    public TranscodingQueue(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void enqueue (TranscodeJob job) {
        var data = new HashMap<String, String>();
        data.put("videoId", job.getVideoId());
        data.put("type", job.getType());
        data.put("uploadId", job.getUploadId());
        data.put("input", job.getInput());

        MapRecord<String, String, String> record = StreamRecords.newRecord()
                .ofStrings(data).withStreamKey("transcoding_stream");
        redisTemplate.opsForStream().add(record);
    }
}
