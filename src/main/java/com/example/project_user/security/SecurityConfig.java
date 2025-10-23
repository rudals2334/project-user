package com.example.project_user.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpMethod;

@Configuration
public class SecurityConfig {
    private final RestAuthHandlers restAuthHandlers;
    private final JwtTokenProvider jwtTokenProvider;

    public SecurityConfig(RestAuthHandlers restAuthHandlers, JwtTokenProvider jwtTokenProvider) {
        this.restAuthHandlers = restAuthHandlers;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(eh -> eh
                .authenticationEntryPoint(restAuthHandlers)
                .accessDeniedHandler(restAuthHandlers)
            )
            .authorizeHttpRequests(auth -> auth
                // 정적 리소스 및 SPA 엔트리포인트는 모두 허용
                .requestMatchers(HttpMethod.GET, "/", "/index.html", "/assets/**", "/favicon.ico", "/logo.png", "/vite.svg").permitAll()
                // 공개 엔드포인트
                .requestMatchers("/health", "/auth/**", "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 다른 사용자 루틴 읽기 전용 공개(요구사항) + 닉네임 조회 공개
                .requestMatchers(HttpMethod.GET, "/routines", "/routines/*", "/members/lookup").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
