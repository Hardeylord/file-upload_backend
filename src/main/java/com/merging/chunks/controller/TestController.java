package com.merging.chunks.controller;

import com.merging.chunks.filter.OAuth2FailureHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    private final OAuth2FailureHandler failureHandler;

    public TestController(OAuth2FailureHandler failureHandler) {
        this.failureHandler = failureHandler;
    }

    @GetMapping("/debug/fail")
    public void simulateFailure(HttpServletRequest req, HttpServletResponse res) throws Exception {
        OAuth2Error error = new OAuth2Error("access_denied", "User denied access", null);
        failureHandler.onAuthenticationFailure(req, res,
                new OAuth2AuthenticationException(error, "simulated failure"));
    }

}
