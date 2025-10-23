package com.example.project_user.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    ROUTINE_DUPLICATE("ROUTINE_DUPLICATE", HttpStatus.CONFLICT, "Routine already exists"),
    USER_DUPLICATE("USER_DUPLICATE", HttpStatus.CONFLICT, "Email or nickname already used"),
    NOT_FOUND("NOT_FOUND", HttpStatus.NOT_FOUND, "Resource not found"),
    FORBIDDEN("FORBIDDEN", HttpStatus.FORBIDDEN, "Forbidden"),
    VALIDATION_FAILED("VALIDATION_FAILED", HttpStatus.BAD_REQUEST, "Validation failed"),
    AUTH_REQUIRED("AUTH_REQUIRED", HttpStatus.UNAUTHORIZED, "Authentication required"),
    SERVER_ERROR("SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "Server error");

    private final String code;
    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(String code, HttpStatus httpStatus, String defaultMessage) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}


