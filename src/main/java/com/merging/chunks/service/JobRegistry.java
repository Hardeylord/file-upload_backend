package com.merging.chunks.service;

import com.merging.chunks.dto.TranscodeJob;
import com.merging.chunks.enums.JOB;
import com.merging.chunks.enums.STATUS;
import com.merging.chunks.model.JobRecord;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

@Component
public class JobRegistry {
    private final RedisTemplate<String, Object> redisTemplate;

    public JobRegistry(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void createRedisJob(JobRecord jobRecord) {
        var fields = new HashMap<String, String>();
        String hashKey = key(jobRecord.id());
        fields.put("id", jobRecord.id().toString());
        fields.put("type", jobRecord.type());
        fields.put("status", jobRecord.status());
        fields.put("attempts", String.valueOf(jobRecord.attempts()));
        fields.put("maxAttempts", String.valueOf(jobRecord.maxAttempts()));
        fields.put("payload", jobRecord.payload());
        fields.put("result", null);
        fields.put("error", null);
        fields.put("createdAt", jobRecord.createdAt().toString());
        fields.put("updatedAt", jobRecord.updatedAt().toString());

        redisTemplate.opsForHash().putAll(hashKey, fields);
    }

    private String key(UUID id) {
        return "transcode:job:"+id;
    }
}
