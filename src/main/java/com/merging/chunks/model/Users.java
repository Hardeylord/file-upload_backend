package com.merging.chunks.model;

import com.merging.chunks.enums.ROLES;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "Users")
@Entity
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(unique = true, nullable = false)
    private String username;
    @Column(unique = true)
    private String password;
    @Column(unique = true, nullable = false)
    private String email;
    @Enumerated(EnumType.STRING)
    private ROLES roles;
    private boolean verified = false;
    @CreationTimestamp
    @Column(nullable = false)
    private Instant created_at;
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updated_at;
}
