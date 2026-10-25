package com.askepedia.controller;

import com.askepedia.dto.HealthResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api")
public class HealthController {

    private static final String SERVICE_NAME = "Askepedia API";
    private static final String VERSION = "0.1.0-SNAPSHOT";

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> checkHealth() {
        HealthResponse response = new HealthResponse(
            "UP",
            SERVICE_NAME,
            VERSION,
            Instant.now()
        );
        return ResponseEntity.ok(response);
    }
}
