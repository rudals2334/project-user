// com/hrt/health_routine_tracker/config/GlobalExceptionHandler.java
package com.example.project_user.exception;

import com.example.project_user.dto.ApiResponse;
import com.example.project_user.exception.BusinessException;
import com.example.project_user.exception.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 계획서 오류 코드 체계에 따른 전역 예외 핸들링 (커스텀 예외 기반)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusiness(BusinessException e) {
        ErrorCode code = e.getErrorCode();
        HttpStatus status = code.getHttpStatus();
        String message = e.getMessage() != null ? e.getMessage() : code.getDefaultMessage();
        return ResponseEntity.status(status)
                .body(ApiResponse.error(code.getCode(), message, e.getDetails()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException e) {
        Map<String, Object> details = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> {
            details.put(error.getField(), error.getDefaultMessage());
        });
        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.VALIDATION_FAILED.getCode(), "Validation failed", details));
    }

    /** JSON 파싱/바인딩 오류 → 400 */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> handleNotReadable(HttpMessageNotReadableException e) {
        return ResponseEntity.status(ErrorCode.VALIDATION_FAILED.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.VALIDATION_FAILED.getCode(), "Invalid or unreadable JSON payload"));
    }

    /** DB 제약조건 위반(UK 등) → 409 */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDataIntegrity(DataIntegrityViolationException e) {
        return ResponseEntity.status(ErrorCode.USER_DUPLICATE.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.USER_DUPLICATE.getCode(), ErrorCode.USER_DUPLICATE.getDefaultMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleEtc(Exception e) {
        // 디버그 편의를 위해 메세지를 그대로 노출(개발용)
        return ResponseEntity.status(ErrorCode.SERVER_ERROR.getHttpStatus())
                .body(ApiResponse.error(ErrorCode.SERVER_ERROR.getCode(), e.getMessage()));
    }
}

