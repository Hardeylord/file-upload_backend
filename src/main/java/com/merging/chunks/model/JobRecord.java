package com.merging.chunks.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public record JobRecord(
        UUID id,
        String type,
        String status,
        int attempts,
        int maxAttempts,
        String payload,
        String result,
        String error,
        Instant createdAt,
        Instant updatedAt
) {
//    public Map<String, String> toMap() {
//        Map<String, String> map = new HashMap<>();
//
//        map.put("id", id.toString());
//        map.put("type", type);
//        map.put("status", status);
//        map.put("attempts", String.valueOf(attempts));
//        map.put("maxAttempts", String.valueOf(maxAttempts));
//        map.put("payload", payload);
//        map.put("result", result);
//        if (error != null)
//            map.put("error", error);
//
//        map.put("createdAt", createdAt.toString());
//        map.put("updatedAt", updatedAt.toString());
//
//        return map;
//    }
//
//    public static JobRecord fromMap(Map<String, String> map) {
//        return new JobRecord(
//                UUID.fromString(map.get("id")),
//                map.get("type"),
//                map.get("status"),
//                Integer.parseInt(map.get("attempts")),
//                Integer.parseInt(map.get("maxAttempts")),
//                map.get("payload"),
//                map.get("result"),
//                map.get("error"),
//                Instant.parse(map.get("createdAt")),
//                Instant.parse(map.get("updatedAt"))
//        );
//    }
}
