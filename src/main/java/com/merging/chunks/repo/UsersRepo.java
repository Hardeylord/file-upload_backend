package com.merging.chunks.repo;

import com.merging.chunks.dto.MyUserDetails;
import com.merging.chunks.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface UsersRepo extends JpaRepository<Users, String> {

    Optional<Users>findById(UUID id);

    @Query("""
            SELECT new com.merging.chunks.dto.MyUserDetails(
            u.id,
            u.username,
            u.password,
            u.roles
            )
            FROM Users u
            WHERE u.username = :username
           """)
    Optional<MyUserDetails> findByUsername(@Param("username") String username);

    Optional<Users> findByEmail(String email);

    @Query("""
            SELECT new com.merging.chunks.dto.MyUserDetails(
            u.id,
            u.username,
            u.password,
            u.roles
            )
            FROM Users u
            WHERE u.username = :username OR u.email = :username
           """)
    Optional<MyUserDetails> findByUsernameOrEmail(@Param("username") String username);


    @Query("""
            SELECT u
            FROM Users u
            WHERE u.username = :username OR u.email = :username
           """)
    Optional<Users> findByUsernameOrEmailRT(@Param("username") String username);
}
