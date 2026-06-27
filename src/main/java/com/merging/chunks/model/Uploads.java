package com.merging.chunks.model;

import com.merging.chunks.enums.STATUS;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@Table(name = "uploads")
@Entity
public class Uploads {
    @Id
    @Column(name = "upload_id")
    private String uploadId;
    @Column(name = "file_name")
    private String fileName;
    @Column(name = "file_size", columnDefinition = "BIGINT")
    private BigInteger fileSize;
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private STATUS status;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Uploads uploads = (Uploads) o;
        return getUploadId() != null && Objects.equals(getUploadId(), uploads.getUploadId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
