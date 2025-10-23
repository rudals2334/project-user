// com/hrt/health_routine_tracker/dto/ApiResponse.java
package com.example.project_user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 표준 응답 포맷 (계획서 오류 코드 체계 지원)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private String code;        // 오류 코드
    private Object details;     // 추가 세부사항

    // 기존 호환성 유지 생성자
    public ApiResponse(boolean success, T data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, message);
    }
    
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null);
    }
    
    public static <T> ApiResponse<T> okMsg(String message) {
        return new ApiResponse<>(true, null, message);
    }
    
    public static <T> ApiResponse<T> okMessage(String message) {
        return new ApiResponse<>(true, null, message);
    }
    
    // 기존 단순 오류
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, null, message);
    }

    // 계획서 오류 포맷: { "code": "ROUTINE_DUPLICATE", "message": "...", "details": {...} }
    public static <T> ApiResponse<T> error(String code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setCode(code);
        response.setMessage(message);
        return response;
    }

    public static <T> ApiResponse<T> error(String code, String message, Object details) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setCode(code);
        response.setMessage(message);
        response.setDetails(details);
        return response;
    }

    public static <T> ApiResponse<T> error(String code, Object details) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setCode(code);
        response.setDetails(details);
        return response;
    }
}
