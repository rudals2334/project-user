// com/hrt/health_routine_tracker/controller/HealthCheckController.java
package com.example.project_user.controller;

import com.example.project_user.dto.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/health")
public class HealthCheckController {
    @GetMapping
    public ApiResponse<?> health() {
        return ApiResponse.okMsg("OK"); // okMessage도 존재(호환)
    }
}
