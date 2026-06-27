package com.merging.chunks.model;

import com.merging.chunks.enums.ChunkStatus;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "file_chunks")
@Entity
public class Chunks {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "partnumber")
    private Integer partNumber;
    @Column(name = "upload_id")
    private String uploadId;
    @Column(name="status")
    @Enumerated(EnumType.STRING)
    private ChunkStatus status;
    @Column(name = "e_tag")
    private String etag;
    @Column(name = "filename")
    private String filename;

    public Chunks(Integer partNumber, String uploadId, ChunkStatus status, String etag, String filename) {
        this.partNumber = partNumber;
        this.uploadId = uploadId;
        this.status = status;
        this.etag = etag;
        this.filename = filename;
    }
}
