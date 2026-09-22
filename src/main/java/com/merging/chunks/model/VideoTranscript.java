package com.merging.chunks.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Table(name = "videotranscript")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class VideoTranscript {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID id;
    @Column(name = "start_offset")
    private double start;
    @Column(name = "end_offset")
    private double end;
    private String text;
    @ManyToOne
    @JoinColumn(name = "video_id", referencedColumnName = "id")
    private Video video;
}
