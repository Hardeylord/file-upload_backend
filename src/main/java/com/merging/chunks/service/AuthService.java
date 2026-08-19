package com.merging.chunks.service;

import com.merging.chunks.dto.MyUserDetails;
import com.merging.chunks.dto.apiresponse.ApiResponse;
import com.merging.chunks.enums.ROLES;
import com.merging.chunks.model.Users;
import com.merging.chunks.repo.RefreshTokenRepo;
import com.merging.chunks.repo.UsersRepo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
public class AuthService {
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
    private final UsersRepo usersRepo;
    private final RefreshTokenRepo refreshTokenRepo;
    private final AuthenticationManager authManager;
    private final JWTService jwtService;
    private final RefreshTokenService refreshTokenService;
    public AuthService(UsersRepo usersRepo, RefreshTokenRepo refreshTokenRepo, AuthenticationManager authManager, JWTService jwtService, RefreshTokenService refreshTokenService) {
        this.usersRepo = usersRepo;
        this.refreshTokenRepo = refreshTokenRepo;
        this.authManager = authManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    public ApiResponse<String> signUpUser(String password, String email, HttpServletResponse response) {
        String username =generateUniqueUsername(email);
        Users user = new Users();
        user.setUsername(username);
        user.setPassword(encoder.encode(password));
        user.setEmail(email);
        user.setRoles(ROLES.ROLE_GUEST);
        try {
            usersRepo.save(user);
            log.info("ACCOUNT CREATED : {}", username);
            String access_token = jwtService.generateToken(username, ROLES.ROLE_GUEST.name());
            String refresh_token = refreshTokenService.generateRefreshToken(user);
            ResponseCookie responseCookie = ResponseCookie
                    .from("refresh_token", refresh_token)
                    .httpOnly(true)
                    .secure(false)
                    .maxAge(Duration.of(7, ChronoUnit.DAYS))
                    .path("/")
                    .sameSite("Lax")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
            return ApiResponse.ok("ACCOUNT CREATED", access_token);
        } catch (DataIntegrityViolationException e) {
            log.info("ACCOUNT NOT CREATED 'DUPLICATE USERNAME : ' : {}", username);
            throw new RuntimeException(e);
        }
    }

    public ApiResponse<String> loginUser(String username, String password, HttpServletResponse response) {
        UsernamePasswordAuthenticationToken uNamePwdAuthTokn = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authManager.authenticate(uNamePwdAuthTokn);
        MyUserDetails userDetails = (MyUserDetails) authentication.getPrincipal();

        String authority = authentication.getAuthorities().iterator().next().getAuthority();
        String access_token = jwtService.generateToken(username, authority);
        String refresh_token = refreshTokenService.generateRefreshToken(findUser(userDetails.getId()));

        ResponseCookie responseCookie = ResponseCookie
                .from("refresh_token", refresh_token)
                .httpOnly(true)
                .secure(false)
                .maxAge(Duration.of(7, ChronoUnit.DAYS))
                .path("/")
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
        return ApiResponse.ok("LOGIN SUCCESSFUL", access_token);
    }

    private Users findUser(UUID id) {
        return usersRepo.findById(id).orElseThrow(()-> new RuntimeException("User Not found, Invalid credentials"));
    }

    private String generateUniqueUsername(String email) {
        String base = email.substring(0, email.indexOf("@"));
        String candidate = base;
        int suffix = 1;
        while (usersRepo.findByUsername(candidate).isPresent()) {
            candidate = base + suffix++;
        }
        return candidate;
    }

}
