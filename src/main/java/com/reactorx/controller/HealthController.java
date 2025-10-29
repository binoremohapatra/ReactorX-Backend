package com.reactorx.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Basic Health Check Controller (Liveness Probe).
 * This endpoint verifies the application process is running and accessible.
 */
@RestController
public class HealthController {

    /**
     * Responds to the conventional REST health endpoint: /api/health
     */
    @GetMapping("/api/health")
    public ResponseEntity<String> checkHealth() {
        // Returns a static 200 OK status with the body "OK"
        return ResponseEntity.ok("OK");
    }
}
