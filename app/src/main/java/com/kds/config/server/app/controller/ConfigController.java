package com.kds.config.server.app.controller;

import com.kds.config.server.app.api.ConfigAPI;
import com.kds.config.server.app.dto.request.ConfigListRequest;
import com.kds.config.server.app.dto.request.ConfigRequest;
import com.kds.config.server.app.dto.response.ConfigListResponse;
import com.kds.config.server.app.dto.response.ConfigResponse;
import com.kds.config.server.app.exception.ConfigAPIException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/configs")
@RequiredArgsConstructor
public class ConfigController {

    private final ConfigAPI configAPI;

    @GetMapping("/{application}/{profile}/{label}/{key}")
    public ResponseEntity<?> getConfig(
            @PathVariable String application,
            @PathVariable String profile,
            @PathVariable String label,
            @PathVariable String key) {
        try {
            return ResponseEntity.ok(configAPI.getConfig(application, profile, label, key));
        } catch (ConfigAPIException e) {
            return ResponseEntity.status(getHttpStatus(e.getStatus()))
                    .body(new ErrorResponse(e.getStatus(), e.getMessage()));
        }
    }

    @GetMapping("/{application}/{profile}/{label}")
    public ResponseEntity<?> getConfigs(
            @PathVariable String application,
            @PathVariable String profile,
            @PathVariable String label) {
        try {
            return ResponseEntity.ok(configAPI.getConfigs(application, profile, label));
        } catch (ConfigAPIException e) {
            return ResponseEntity.status(getHttpStatus(e.getStatus()))
                    .body(new ErrorResponse(e.getStatus(), e.getMessage()));
        }
    }

    @GetMapping("/{application}/{profile}")
    public ResponseEntity<?> getConfigs(
            @PathVariable String application,
            @PathVariable String profile) {
        try {
            return ResponseEntity.ok(configAPI.getConfigs(application, profile));
        } catch (ConfigAPIException e) {
            return ResponseEntity.status(getHttpStatus(e.getStatus()))
                    .body(new ErrorResponse(e.getStatus(), e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> saveConfig(@RequestBody ConfigRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(configAPI.saveConfig(request));
        } catch (ConfigAPIException e) {
            return ResponseEntity.status(getHttpStatus(e.getStatus()))
                    .body(new ErrorResponse(e.getStatus(), e.getMessage()));
        }
    }

    @PostMapping("/batch")
    public ResponseEntity<?> saveConfigs(@RequestBody ConfigListRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(configAPI.saveConfigs(request));
        } catch (ConfigAPIException e) {
            return ResponseEntity.status(getHttpStatus(e.getStatus()))
                    .body(new ErrorResponse(e.getStatus(), e.getMessage()));
        }
    }

    @DeleteMapping("/{application}/{profile}/{label}/{key}")
    public ResponseEntity<?> deleteConfig(
            @PathVariable String application,
            @PathVariable String profile,
            @PathVariable String label,
            @PathVariable String key) {
        try {
            configAPI.deleteConfig(application, profile, label, key);
            return ResponseEntity.noContent().build();
        } catch (ConfigAPIException e) {
            return ResponseEntity.status(getHttpStatus(e.getStatus()))
                    .body(new ErrorResponse(e.getStatus(), e.getMessage()));
        }
    }

    private HttpStatus getHttpStatus(String status) {
        return switch (status) {
            case "NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "CONFLICT" -> HttpStatus.CONFLICT;
            case "BAD_REQUEST" -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private record ErrorResponse(String status, String message) {}
} 