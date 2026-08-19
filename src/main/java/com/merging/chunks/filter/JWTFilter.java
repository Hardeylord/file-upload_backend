package com.merging.chunks.filter;

import com.merging.chunks.service.JWTService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JWTFilter extends OncePerRequestFilter {
    private final JWTService jwtService;

    public JWTFilter(JWTService jwtService) {
        this.jwtService = jwtService;
    }
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/signUp",
            "/api/v1/auth/refreshToken",
            "/api/v1/videos",
            "/api/v1/video/*",
            "/api/v1/categories",
            "/debug/fail"
    );
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return PUBLIC_PATHS.stream().anyMatch(path::equals);
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        String token = null;
        if (header == null || !header.startsWith("Bearer")) {
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            String jsonResponse = """ 
                {
                    "status": %s,
                    "error": "Invalid Token.",
                    "message": "Login to continue"
                }
                """.formatted(HttpStatus.SC_UNAUTHORIZED);
            response.getWriter().write(jsonResponse);
            return;
        }
        token=header.substring(7);
        String username = jwtService.extractUsername(token);
        Claims claims;
        try {
            claims = jwtService.verifyExtractClaims(token);
//            System.out.println(claims);
            if (username == null || jwtService.isTokenExpired(token)) {
                response.setStatus(HttpStatus.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                String jsonResponse = """ 
                {
                    "status": %s,
                    "error": "Token Expired.",
                    "message": "Login to continue"
                }
                """.formatted(HttpStatus.SC_UNAUTHORIZED);
                response.getWriter().write(jsonResponse);
                return;
            }
            String role = claims.get("Role", String.class);
//            System.out.println(role);
            List<SimpleGrantedAuthority> authority = List.of(new SimpleGrantedAuthority(role));

            if (SecurityContextHolder.getContext().getAuthentication() != null) {
                response.setStatus(HttpStatus.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                String jsonResponse = """ 
                {
                    "status": %s,
                    "error": "Error.",
                    "message": "Logout & Login to continue"
                }
                """.formatted(HttpStatus.SC_UNAUTHORIZED);
                response.getWriter().write(jsonResponse);
                return;
            }
            UsernamePasswordAuthenticationToken uNamePwdAuthTokn =
                    new UsernamePasswordAuthenticationToken(username
                            , null, authority);

            SecurityContextHolder.getContext().setAuthentication(uNamePwdAuthTokn);
        } catch (JwtException e) {
            response.setStatus(HttpStatus.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            String jsonResponse = """ 
                {
                    "status": %s,
                    "error": "Invalid Token.",
                    "message": "Login to continue"
                }
                """.formatted(HttpStatus.SC_UNAUTHORIZED);
            response.getWriter().write(jsonResponse);
            return;
        }
            filterChain.doFilter(request, response);
    }
}
