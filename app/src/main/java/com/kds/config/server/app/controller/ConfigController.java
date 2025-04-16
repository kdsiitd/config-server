package com.kds.config.server.app.controller;

import com.kds.config.server.app.api.ConfigAPI;
import com.kds.config.server.app.dto.request.ConfigListRequest;
import com.kds.config.server.app.dto.request.ConfigRequest;
import com.kds.config.server.app.dto.response.ConfigListResponse;
import com.kds.config.server.app.dto.response.ConfigResponse;
import com.kds.config.server.app.exception.ConfigAPIException;
import com.kds.config.server.core.entity.Config;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.core.env.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/configs")
@RequiredArgsConstructor
@Tag(name = "Configuration Management", description = "APIs for managing application configurations")
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
            ConfigListResponse listResponse = configAPI.getConfigs(application, profile, label);
            return getResponseEntity(application, profile, label, listResponse);
        } catch (ConfigAPIException e) {
            return ResponseEntity.status(getHttpStatus(e.getStatus()))
                    .body(new ErrorResponse(e.getStatus(), e.getMessage()));
        }
    }

    @GetMapping("/{application}/{profile}")
    public ResponseEntity<?> getConfig(
            @PathVariable String application,
            @PathVariable String profile) {
        try {
            ConfigListResponse listResponse = configAPI.getConfigs(application, profile);
            return getResponseEntity(application, profile, null, listResponse);
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

    /**
     * Updates a single configuration property.
     *
     * @param request The configuration update request containing application, profile, label, key, and new value
     * @return ResponseEntity containing the updated configuration or error response
     */
    @Operation(
        summary = "Update a configuration property",
        description = "Updates the value of an existing configuration property. The property must exist."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configuration updated successfully",
            content = @Content(schema = @Schema(implementation = ConfigResponse.class))),
        @ApiResponse(responseCode = "404", description = "Configuration not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping
    public ResponseEntity<?> updateConfig(@RequestBody ConfigRequest request) {
        try {
            return ResponseEntity.ok(configAPI.updateConfig(request));
        } catch (ConfigAPIException e) {
            return ResponseEntity.status(getHttpStatus(e.getStatus()))
                    .body(new ErrorResponse(e.getStatus(), e.getMessage()));
        }
    }

    /**
     * Updates multiple configuration properties in a batch.
     *
     * @param request The batch configuration update request containing multiple configurations
     * @return ResponseEntity containing the list of updated configurations or error response
     */
    @Operation(
        summary = "Update multiple configuration properties",
        description = "Updates multiple configuration properties in a single request. All properties must exist."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Configurations updated successfully",
            content = @Content(schema = @Schema(implementation = ConfigListResponse.class))),
        @ApiResponse(responseCode = "404", description = "One or more configurations not found",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/batch")
    public ResponseEntity<?> updateConfigs(@RequestBody ConfigListRequest request) {
        try {
            return ResponseEntity.ok(configAPI.updateConfigs(request));
        } catch (ConfigAPIException e) {
            return ResponseEntity.status(getHttpStatus(e.getStatus()))
                    .body(new ErrorResponse(e.getStatus(), e.getMessage()));
        }
    }

    private ResponseEntity<?> getResponseEntity(String applicationName, String profile, String label,
                                                ConfigListResponse listResponse) {
        Map<String, Object> response = new LinkedHashMap<>();
        if(!Strings.isEmpty(applicationName)) response.put("name", applicationName);
        if(!Strings.isEmpty(profile)) response.put("profiles", new String[]{profile});
        if(!Strings.isEmpty(label)) response.put("label", label);

        Map<String, Object> source = new HashMap<>();
        for (Config config : listResponse.getConfigs()) {
            source.put(config.getPropKey(), config.getPropValue());
        }

        List<PropertySource<?>> propertySources = new ArrayList<>();
        PropertySource<?> propertySource = new PropertySource<>(
                "throttler-service-prod",
                source
        ) {
            @Override
            public Object getProperty(String name) {
                StringBuilder builder = new StringBuilder();
                if(!Strings.isEmpty(applicationName)) builder.append(applicationName);
                if(!Strings.isEmpty(profile)) builder.append("-").append(profile);
                if(!Strings.isEmpty(label)) builder.append("-").append(label);

                return builder.toString();
            }
        };

        propertySources.add(propertySource);

        response.put("propertySources", propertySources);

        return ResponseEntity.ok(response);
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