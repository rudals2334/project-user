package com.example.project_user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${cors.allowed-origins:http://localhost:3000,http://127.0.0.1:3000}")
    private String allowedOriginsProp;
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        String[] origins = allowedOriginsProp.split(",");
        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS")
                .allowedHeaders("Authorization", "Content-Type", "X-Requested-With", "Accept", "Origin")
                .exposedHeaders("Location", "Content-Disposition")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /** 간단한 요청 로깅 필터 (개발 환경용) */
    @Bean
    public Filter requestLoggingFilter() {
        return new Filter() {
            @Override
            public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response, FilterChain chain) throws IOException, ServletException {
                HttpServletRequest req = (HttpServletRequest) request;
                long start = System.currentTimeMillis();
                try {
                    chain.doFilter(request, response);
                } finally {
                    long ms = System.currentTimeMillis() - start;
                    HttpServletResponse res = (HttpServletResponse) response;
                    System.out.println("[HTTP] " + req.getMethod() + " " + req.getRequestURI() + " -> " + res.getStatus() + " (" + ms + "ms)");
                }
            }
        };
    }
}
