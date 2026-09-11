package com.merging.chunks.config;

import com.merging.chunks.filter.JWTFilter;
import com.merging.chunks.filter.OAuth2FailureHandler;
import com.merging.chunks.filter.OAuth2SuccessHandler;
import com.merging.chunks.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JWTFilter jwtFilter;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final OAuth2FailureHandler oAuth2FailureHandler;

    public SecurityConfig(CustomUserDetailsService userDetailsService, JWTFilter jwtFilter, OAuth2SuccessHandler oAuth2SuccessHandler, OAuth2FailureHandler oAuth2FailureHandler) {
        this.userDetailsService = userDetailsService;
        this.jwtFilter = jwtFilter;
        this.oAuth2SuccessHandler = oAuth2SuccessHandler;
        this.oAuth2FailureHandler = oAuth2FailureHandler;
    }

    @Bean
    public SecurityFilterChain filterChain (HttpSecurity httpSecurity) {
       return httpSecurity
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req->
                    req
                            .requestMatchers("/api/v1/auth/login",
                                    "/api/v1/auth/signUp",
                                    "/api/v1/auth/refreshToken",
                                    "/api/v1/videos",
                                    "/api/v1/search",
                                    "/api/v1/video/*",
                                    "/api/v1/categories",
                                    "/api/v1/publishStream")
                            .permitAll()
                            .anyRequest().authenticated()
                )
               .exceptionHandling(exception->
                       exception.defaultAuthenticationEntryPointFor(
                               new HttpStatusEntryPoint(
                                       HttpStatus.UNAUTHORIZED
                               ),
                               request -> request
                                       .getRequestURI()
                                       .startsWith("/file/")
                       ))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .oauth2Login(oAuth2->{
                       oAuth2.successHandler(oAuth2SuccessHandler);
                       oAuth2.failureHandler(oAuth2FailureHandler);
                })
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationProvider authProvider () {
        DaoAuthenticationProvider daoAP = new DaoAuthenticationProvider(userDetailsService);
        daoAP.setPasswordEncoder(passwordEncoder());
        return daoAP;
    }

    @Bean
    public AuthenticationManager authManager(AuthenticationConfiguration authConfig) {
        return authConfig.getAuthenticationManager();
    }
}
