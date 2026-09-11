package com.merging.chunks.service;

import com.merging.chunks.model.RefreshToken;
import com.merging.chunks.model.Users;
import com.merging.chunks.repo.RefreshTokenRepo;
import com.merging.chunks.repo.UsersRepo;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@Component
public class RefreshTokenService {
    private final UsersRepo usersRepo;
    private final RefreshTokenRepo refreshTokenRepo;
    private final JWTService jwtService;

    public RefreshTokenService(UsersRepo usersRepo, RefreshTokenRepo refreshTokenRepo, JWTService jwtService) {
        this.usersRepo = usersRepo;
        this.refreshTokenRepo = refreshTokenRepo;
        this.jwtService = jwtService;
    }

    public String generateRefreshToken(Users user) {
            String refreshToken = generateRefreshTokenString();
            String RTHash = hashRefreshTokenString(refreshToken);
            RefreshToken refreshTokenRow = new RefreshToken();
            refreshTokenRow.setUser(user);
            refreshTokenRow.setExpires_at(Instant.now().plus(7, ChronoUnit.DAYS));
            refreshTokenRow.setToken_hash(RTHash);
            refreshTokenRepo.save(refreshTokenRow);

            return refreshToken;
    }

//    findByToken and Token Rotation
    public String findByToken(String token, HttpServletResponse response) {
        String refreshToken =hashRefreshTokenString(token);
        System.out.println(refreshToken);
        RefreshToken refreshTokenRow = refreshTokenRepo.findByTokenHashNotRevoked(refreshToken).orElseThrow(()-> new RuntimeException("INVALID TOKEN"));

        if (isExpire(refreshTokenRow)) throw new RuntimeException("TOKEN EXPIRED LOGIN TO CONTINUE");

//        revoke token [it is the old token sent from cookie]
        refreshTokenRow.setRevoked(true);
        refreshTokenRepo.save(refreshTokenRow);

        String access_token=jwtService.generateToken(refreshTokenRow.getUser().getUsername(), refreshTokenRow.getUser().getRoles().name());
        String newToken = generateRefreshTokenString();
        ResponseCookie responseCookie = ResponseCookie
                .from("refresh_token", newToken)
                .httpOnly(true)
                .secure(true)
                .maxAge(Duration.of(7, ChronoUnit.DAYS))
                .path("/")
                .sameSite("None")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
        rotateRefreshToken(refreshTokenRow.getUser(), newToken);
        return access_token;
    }

    private void rotateRefreshToken(Users user, String newRfreshToken) {
        String hashedRefreshToken = hashRefreshTokenString(newRfreshToken);
        RefreshToken rotateRefreshToken = new RefreshToken();
        rotateRefreshToken.setUser(user);
        rotateRefreshToken.setToken_hash(hashedRefreshToken);
        rotateRefreshToken.setExpires_at(Instant.now().plus(7, ChronoUnit.DAYS));
        refreshTokenRepo.save(rotateRefreshToken);
    }

    private boolean isExpire (RefreshToken refreshToken) {
        return refreshToken.getExpires_at().isBefore(Instant.now());
    }

    private String generateRefreshTokenString() {
        byte[] randomBytes = new byte[32];

        new SecureRandom().nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    private String hashRefreshTokenString(String token) {

        try {
            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(
                    "SHA-256 algorithm not available", e
            );
        }
    }
}
