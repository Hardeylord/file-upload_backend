package com.merging.chunks.metadataDTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class Metadata {
    public File file;
    public Video video;
    public Audio audio;
    public Hls hls;
    public String generatedAt;
}
