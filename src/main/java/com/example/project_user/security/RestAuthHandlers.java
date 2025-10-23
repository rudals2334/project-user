package com.example.project_user.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.project_user.dto.ApiResponse;
import com.example.project_user.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RestAuthHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, org.springframework.security.core.AuthenticationException authException) throws IOException {
        write(response, ErrorCode.AUTH_REQUIRED);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        write(response, ErrorCode.FORBIDDEN);
    }

    private void write(HttpServletResponse res, ErrorCode code) throws IOException {
        res.setStatus(code.getHttpStatus().value());
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.getWriter().write(mapper.writeValueAsString(ApiResponse.error(code.getCode(), code.getDefaultMessage())));
    }
}


