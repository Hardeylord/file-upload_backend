package com.merging.chunks.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class JWTDto {
    public String access_token;
    public String refresh_token;
}
