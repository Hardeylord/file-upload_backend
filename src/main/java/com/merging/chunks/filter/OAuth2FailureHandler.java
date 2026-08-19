package com.merging.chunks.filter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        logger.warn("OAuth2 authentication failed: {} "+exception.getMessage(), exception);

        String errorMessage = resolveErrorMessage(exception);

        String redirectUrl = UriComponentsBuilder
                .fromUriString("http://localhost:5173/registration/login")
                .queryParam("error", URLEncoder.encode(errorMessage, StandardCharsets.UTF_8))
                .build()
                .toUriString();

        response.sendRedirect(redirectUrl);
    }

    private String resolveErrorMessage(AuthenticationException exception) {
        if (exception instanceof OAuth2AuthenticationException oAuth2Ex) {
            OAuth2Error error = oAuth2Ex.getError();
            return error != null ? error.getErrorCode() : "oauth2_error";
        }
        return "authentication_failed";
    }
}
