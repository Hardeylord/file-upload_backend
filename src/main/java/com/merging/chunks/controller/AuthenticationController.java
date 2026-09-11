package com.merging.chunks.controller;

import com.merging.chunks.dto.apiresponse.ApiResponse;
import com.merging.chunks.service.AuthService;
import com.merging.chunks.service.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    public AuthenticationController(AuthService authService, RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.refreshTokenService = refreshTokenService;
    }


    @PostMapping("/login")
    public ApiResponse<String> login(@RequestParam("username") String username,
                                     @RequestParam("password") String password,
                                     HttpServletResponse response) {
        return authService.loginUser(username, password, response);
    }

    @PostMapping("/signUp")
    public ApiResponse<String> signUp(@RequestParam("password") String password,
                                      @RequestParam("email") String email,
                                      HttpServletResponse response) {
        return authService.signUpUser(password, email, response);
    }

    @PostMapping("/refreshToken")
    public ApiResponse<String> refreshToken(HttpServletRequest request,
                                            HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        System.out.println("Checking token....");
        if (cookies == null) throw new RuntimeException("NO SET COOKIES ... LOGIN TO CONTINUE");
        String refreshToken = refreshToken = Arrays.stream(cookies)
                    .filter(cookie -> "refresh_token".equals(cookie.getName()))
                    .map(Cookie::getValue)
                    .findFirst()
                    .orElseThrow(()-> new RuntimeException("NO SET COOKIES...Session Expired login again to continue"));

        System.out.println("Found token....");
        System.out.println("Token : "+refreshToken);
        return ApiResponse.ok("VALID TOKEN", refreshTokenService.findByToken(refreshToken, response));
    }
}
