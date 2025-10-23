// com/hrt/health_routine_tracker/dto/PageResponse.java
package com.example.project_user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 페이지네이션 응답 포맷(컨텐츠 + 메타).
 */
@Data
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
}
