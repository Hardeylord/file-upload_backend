package com.merging.chunks.filter;

import com.merging.chunks.enums.ROLES;
import com.merging.chunks.model.Users;
import com.merging.chunks.repo.UsersRepo;
import com.merging.chunks.service.JWTService;
import com.merging.chunks.service.RefreshTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final UsersRepo usersRepo;
    private final JWTService jwtService;
    private final RefreshTokenService refreshTokenService;

    public OAuth2SuccessHandler(UsersRepo usersRepo, JWTService jwtService, RefreshTokenService refreshTokenService) {
        this.usersRepo = usersRepo;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        System.out.println("GOT HERE");
        if (oauthUser == null) {
            response.sendRedirect("/login?error=no_principal");
            return;
        }
        if (oauthUser.getAttribute("name") == null)
        {
            response.sendRedirect("/login?error=no_email");
            return;
        }
        String email =(String) oauthUser.getAttribute("email");

        Optional<Users> user = usersRepo.findByEmail(email);
        String JWTToken;
        String RefreshToken;
        if (user.isPresent()) {
            Users credentials = user.get();
            RefreshToken=refreshTokenService.generateRefreshToken(credentials);
            JWTToken = jwtService.generateToken(credentials.getUsername(), credentials.getRoles().name());
            logger.info("LOGIN WITH GOOGLE SUCCESSFUL : "+credentials.getEmail());
        } else
        {
            assert email != null;
            String username = generateUniqueUsername(email);
            Users addUser = new Users();
            addUser.setUsername(username);
            addUser.setEmail(email);
            addUser.setRoles(ROLES.ROLE_GUEST);
            usersRepo.save(addUser);
            logger.info("SIGN-IN WITH GOOGLE SUCCESSFUL : "+email);
            RefreshToken=refreshTokenService.generateRefreshToken(addUser);
            JWTToken=jwtService.generateToken(username, ROLES.ROLE_GUEST.name());
        }
//        Cookie jwtCookie = new Cookie("jwt", JWTToken);
//        jwtCookie.setHttpOnly(true);
//        jwtCookie.setSecure(true);
//        jwtCookie.setPath("/");
//        jwtCookie.setMaxAge(1800);
        ResponseCookie responseCookie = ResponseCookie
                .from("refresh_token", RefreshToken)
                .httpOnly(true)
                .secure(false)
                .maxAge(Duration.of(7, ChronoUnit.DAYS))
                .path("/")
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, responseCookie.toString());
        response.sendRedirect("http://localhost:5173/lessons/?login=success&type="+JWTToken);
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
