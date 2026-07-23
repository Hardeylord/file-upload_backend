package com.merging.chunks.model;

import com.merging.chunks.metadataDTO.Variants;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "video")
@Builder
@Entity
@AllArgsConstructor
public class Video {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "title")
    private String title;
    @Column(name = "dscription")
    private String description;
    @Column(name = "filename")
    private String filename;
    @Column(name = "status")
    private String status;
    @Column(name = "master_playlist")
    private String masterplaylist;
    @Column(name = "duration")
    private double duration;
    @Column(name = "thumbnail")
    private String thumbnail;
    @Column(name = "size")
    private double sizeMB;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "resolutions", columnDefinition = "jsonb")
    private List<String> resolutions;
}
