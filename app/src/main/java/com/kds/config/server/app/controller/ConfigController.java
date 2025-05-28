package com.kds.config.server.app.controller;

import com.kds.config.server.app.api.ConfigAPI;
import com.kds.config.server.app.dto.request.ConfigListRequest;
import com.kds.config.server.app.dto.request.ConfigRequest;
import com.kds.config.server.app.dto.response.ConfigListResponse;
import com.kds.config.server.app.dto.response.ConfigResponse;
import com.kds.config.server.app.exception.ConfigAPIException;
import com.kds.config.server.core.entity.Config;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.core.env.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * REST Controller for Configuration Management
 * 
 * This controller provides comprehensive APIs for managing application configurations
 * including CRUD operations, batch operations, and Spring Cloud Config compatible endpoints.
 * 
 * @author KDS Team
 * @version 1.0.0
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/configs")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Configuration Management", 
     description = "Comprehensive APIs for managing application configurations with support for Spring Cloud Config format")
public class ConfigController {

    private final ConfigAPI configAPI;

    /**
     * Retrieves a specific configuration property by application, profile, label, and key.
     * 
     * @param application The application name
     * @param profile The environment profile (e.g., dev, test, prod)
     * @param label The version label or branch name
     * @param key The configuration property key
     * @return The configuration value wrapped in a response object
     */
    @Operation(
        summary = "Get a specific configuration property",
        description = "Retrieves a single configuration property value by its unique identifiers. " +
                     "This endpoint is useful for getting specific property values when you know the exact key.",
        tags = {"Configuration Retrieval"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Configuration property retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ConfigResponse.class),
                examples = @ExampleObject(
                    name = "Successful Response",
                    value = """
                        {
                          "status": "SUCCESS",
                          "message": "Config Retrieved",
                          "config": {
                            "id": 1,
                            "application": "user-service",
                            "profile": "prod",
                            "label": "v1.0.0",
                            "propKey": "database.url",
                            "propValue": "jdbc:postgresql://prod-db:5432/userdb",
                            "createdAt": "2024-01-15T10:30:00Z",
                            "updatedAt": "2024-01-15T10:30:00Z"
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Configuration property not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Not Found Error",
                    value = """
                        {
                          "status": "NOT_FOUND",
                          "message": "Configuration not found for application: user-service, profile: prod, label: v1.0.0, key: database.url"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request parameters",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Internal server error",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @GetMapping("/{application}/{profile}/{label}/{key}")
    public ResponseEntity<?> getConfig(
            @Parameter(description = "Application name", example = "user-service", required = true)
            @PathVariable @NotBlank(message = "Application name cannot be blank") String application,
            
            @Parameter(description = "Environment profile", example = "prod", required = true)
            @PathVariable @NotBlank(message = "Profile cannot be blank") String profile,
            
            @Parameter(description = "Version label or branch", example = "v1.0.0", required = true)
            @PathVariable @NotBlank(message = "Label cannot be blank") String label,
            
            @Parameter(description = "Configuration property key", example = "database.url", required = true)
            @PathVariable @NotBlank(message = "Key cannot be blank") String key) {
        
        log.info("Getting config for application: {}, profile: {}, label: {}, key: {}", 
                application, profile, label, key);
        
        try {
            ConfigResponse response = configAPI.getConfig(application, profile, label, key);
            log.info("Successfully retrieved config for key: {}", key);
            return ResponseEntity.ok(response);
        } catch (ConfigAPIException e) {
            log.error("Error retrieving config: {}", e.getMessage());
            return ResponseEntity.status(getHttpStatus(e.getStatus()))
                    .body(new ErrorResponse(e.getStatus(), e.getMessage()));
        }
    }

    /**
     * Retrieves all configuration properties for a specific application, profile, and label.
     * Returns data in Spring Cloud Config compatible format.
     * 
     * @param application The application name
     * @param profile The environment profile
     * @param label The version label
     * @return All configurations in Spring Cloud Config format
     */
    @Operation(
        summary = "Get all configurations for application, profile, and label",
        description = "Retrieves all configuration properties for the specified application, profile, and label. " +
                     "Returns data in Spring Cloud Config compatible format with property sources.",
        tags = {"Configuration Retrieval"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Configurations retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Spring Cloud Config Format",
                    value = """
                        {
                          "name": "user-service",
                          "profiles": ["prod"],
                          "label": "v1.0.0",
                          "propertySources": [
                            {
                              "name": "user-service-prod-v1.0.0",
                              "source": {
                                "database.url": "jdbc:postgresql://prod-db:5432/userdb",
                                "database.username": "produser",
                                "cache.ttl": "3600",
                                "logging.level": "INFO"
                              }
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "No configurations found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @GetMapping("/{application}/{profile}/{label}")
    public ResponseEntity<?> getConfigs(
            @Parameter(description = "Application name", example = "user-service", required = true)
            @PathVariable @NotBlank(message = "Application name cannot be blank") String application,
            
            @Parameter(description = "Environment profile", example = "prod", required = true)
            @PathVariable @NotBlank(message = "Profile cannot be blank") String profile,
            
            @Parameter(description = "Version label or branch", example = "v1.0.0", required = true)
            @PathVariable @NotBlank(message = "Label cannot be blank") String label) {
        
        log.info("Getting configs for application: {}, profile: {}, label: {}", 
                application, profile, label);
        
        try {
            ConfigListResponse listResponse = configAPI.getConfigs(application, profile, label);
            log.info("Successfully retrieved {} configs", listResponse.getConfigs().size());
            return getResponseEntity(application, profile, label, listResponse);
        } catch (ConfigAPIException e) {
            log.error("Error retrieving configs: {}", e.getMessage());
            return ResponseEntity.status(getHttpStatus(e.getStatus()))
                    .body(new ErrorResponse(e.getStatus(), e.getMessage()));
        }
    }

    /**
     * Retrieves all configuration properties for a specific application and profile.
     * Uses default label when label is not specified.
     * 
     * @param application The application name
     * @param profile The environment profile
     * @return All configurations in Spring Cloud Config format
     */
    @Operation(
        summary = "Get all configurations for application and profile",
        description = "Retrieves all configuration properties for the specified application and profile using the default label. " +
                     "This is a convenience endpoint when you don't need to specify a specific label/version.",
        tags = {"Configuration Retrieval"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Configurations retrieved successfully",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "No configurations found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @GetMapping("/{application}/{profile}")
    public ResponseEntity<?> getConfigsByApplicationAndProfile(
            @Parameter(description = "Application name", example = "user-service", required = true)
            @PathVariable @NotBlank(message = "Application name cannot be blank") String application,
            
            @Parameter(description = "Environment profile", example = "prod", required = true)
            @PathVariable @NotBlank(message = "Profile cannot be blank") String profile) {
        
        log.info("Getting configs for application: {}, profile: {}", application, profile);
        
        try {
            ConfigListResponse listResponse = configAPI.getConfigs(application, profile);
            log.info("Successfully retrieved {} configs", listResponse.getConfigs().size());
            return getResponseEntity(application, profile, null, listResponse);
        } catch (ConfigAPIException e) {
            log.error("Error retrieving configs: {}", e.getMessage());
            return ResponseEntity.status(getHttpStatus(e.getStatus()))
                    .body(new ErrorResponse(e.getStatus(), e.getMessage()));
        }
    }

    /**
     * Creates a new configuration property.
     * 
     * @param request The configuration creation request
     * @return The created configuration wrapped in a response object
     */
    @Operation(
        summary = "Create a new configuration property",
        description = "Creates a new configuration property. All fields (application, profile, label, key, value) are required. " +
                     "The combination of application, profile, label, and key must be unique.",
        tags = {"Configuration Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "Configuration created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ConfigResponse.class),
                examples = @ExampleObject(
                    name = "Created Response",
                    value = """
                        {
                          "status": "SUCCESS",
                          "message": "Config Saved",
                          "config": {
                            "id": 1,
                            "application": "user-service",
                            "profile": "prod",
                            "label": "v1.0.0",
                            "propKey": "database.url",
                            "propValue": "jdbc:postgresql://prod-db:5432/userdb",
                            "createdAt": "2024-01-15T10:30:00Z",
                            "updatedAt": "2024-01-15T10:30:00Z"
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request data or validation errors",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409", 
            description = "Configuration already exists",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Conflict Error",
                    value = """
                        {
                          "status": "CONFLICT",
                          "message": "Configuration already exists for the given application, profile, label, and key"
                        }
                        """
                )
            )
        )
    })
    @PostMapping
    public ResponseEntity<?> saveConfig(
            @Parameter(description = "Configuration creation request", required = true)
            @Valid @RequestBody ConfigRequest request) {
        
        log.info("Creating config for application: {}, profile: {}, key: {}", 
                request.getApplication(), request.getProfile(), request.getKey());
        
        try {
            ConfigResponse response = configAPI.saveConfig(request);
            log.info("Successfully created config with key: {}", request.getKey());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ConfigAPIException e) {
            log.error("Error creating config: {}", e.getMessage());
            return ResponseEntity.status(getHttpStatus(e.getStatus()))
                    .body(new ErrorResponse(e.getStatus(), e.getMessage()));
        }
    }

    /**
     * Creates multiple configuration properties in a single batch operation.
     * 
     * @param request The batch configuration creation request
     * @return The created configurations wrapped in a response object
     */
    @Operation(
        summary = "Create multiple configuration properties in batch",
        description = "Creates multiple configuration properties in a single transaction. " +
                     "If any configuration fails to create, the entire batch operation is rolled back. " +
                     "Each configuration must have unique combination of application, profile, label, and key.",
        tags = {"Configuration Management", "Batch Operations"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "All configurations created successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ConfigListResponse.class),
                examples = @ExampleObject(
                    name = "Batch Creation Success",
                    value = """
                        {
                          "status": "SUCCESS",
                          "message": "Configs Saved Successfully",
                          "configs": [
                            {
                              "id": 1,
                              "application": "user-service",
                              "profile": "prod",
                              "label": "v1.0.0",
                              "propKey": "database.url",
                              "propValue": "jdbc:postgresql://prod-db:5432/userdb"
                            },
                            {
                              "id": 2,
                              "application": "user-service",
                              "profile": "prod",
                              "label": "v1.0.0",
                              "propKey": "database.username",
                              "propValue": "produser"
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request data or validation errors",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "409", 
            description = "One or more configurations already exist",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PostMapping("/batch")
    public ResponseEntity<?> saveConfigs(
            @Parameter(description = "Batch configuration creation request", required = true)
            @Valid @RequestBody ConfigListRequest request) {
        
        log.info("Creating {} configs in batch", request.getConfigs().size());
        
        try {
            ConfigListResponse response = configAPI.saveConfigs(request);
            log.info("Successfully created {} configs in batch", response.getConfigs().size());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (ConfigAPIException e) {
            log.error("Error creating configs in batch: {}", e.getMessage());
            return ResponseEntity.status(getHttpStatus(e.getStatus()))
                    .body(new ErrorResponse(e.getStatus(), e.getMessage()));
        }
    }

    /**
     * Deletes a specific configuration property.
     * 
     * @param application The application name
     * @param profile The environment profile
     * @param label The version label
     * @param key The configuration property key
     * @return No content response on successful deletion
     */
    @Operation(
        summary = "Delete a configuration property",
        description = "Permanently deletes a configuration property. This action cannot be undone. " +
                     "Returns 204 No Content on successful deletion.",
        tags = {"Configuration Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204", 
            description = "Configuration deleted successfully"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Configuration not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request parameters",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @DeleteMapping("/{application}/{profile}/{label}/{key}")
    public ResponseEntity<?> deleteConfig(
            @Parameter(description = "Application name", example = "user-service", required = true)
            @PathVariable @NotBlank(message = "Application name cannot be blank") String application,
            
            @Parameter(description = "Environment profile", example = "prod", required = true)
            @PathVariable @NotBlank(message = "Profile cannot be blank") String profile,
            
            @Parameter(description = "Version label or branch", example = "v1.0.0", required = true)
            @PathVariable @NotBlank(message = "Label cannot be blank") String label,
            
            @Parameter(description = "Configuration property key", example = "database.url", required = true)
            @PathVariable @NotBlank(message = "Key cannot be blank") String key) {
        
        log.info("Deleting config for application: {}, profile: {}, label: {}, key: {}", 
                application, profile, label, key);
        
        try {
            configAPI.deleteConfig(application, profile, label, key);
            log.info("Successfully deleted config with key: {}", key);
            return ResponseEntity.noContent().build();
        } catch (ConfigAPIException e) {
            log.error("Error deleting config: {}", e.getMessage());
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
        description = "Updates the value of an existing configuration property. The property must exist. " +
                     "Only the value field can be updated; other identifying fields (application, profile, label, key) " +
                     "are used to locate the configuration to update.",
        tags = {"Configuration Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Configuration updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ConfigResponse.class),
                examples = @ExampleObject(
                    name = "Update Success",
                    value = """
                        {
                          "status": "SUCCESS",
                          "message": "Config Updated",
                          "config": {
                            "id": 1,
                            "application": "user-service",
                            "profile": "prod",
                            "label": "v1.0.0",
                            "propKey": "database.url",
                            "propValue": "jdbc:postgresql://new-prod-db:5432/userdb",
                            "createdAt": "2024-01-15T10:30:00Z",
                            "updatedAt": "2024-01-15T14:45:00Z"
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "Configuration not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class),
                examples = @ExampleObject(
                    name = "Not Found Error",
                    value = """
                        {
                          "status": "NOT_FOUND",
                          "message": "Configuration not found for update"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request data",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PutMapping
    public ResponseEntity<?> updateConfig(
            @Parameter(description = "Configuration update request", required = true)
            @Valid @RequestBody ConfigRequest request) {
        
        log.info("Updating config for application: {}, profile: {}, key: {}", 
                request.getApplication(), request.getProfile(), request.getKey());
        
        try {
            ConfigResponse response = configAPI.updateConfig(request);
            log.info("Successfully updated config with key: {}", request.getKey());
            return ResponseEntity.ok(response);
        } catch (ConfigAPIException e) {
            log.error("Error updating config: {}", e.getMessage());
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
        summary = "Update multiple configuration properties in batch",
        description = "Updates multiple configuration properties in a single transaction. " +
                     "All properties must exist. If any update fails, the entire batch operation is rolled back. " +
                     "This is useful for updating related configuration properties atomically.",
        tags = {"Configuration Management", "Batch Operations"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "All configurations updated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ConfigListResponse.class),
                examples = @ExampleObject(
                    name = "Batch Update Success",
                    value = """
                        {
                          "status": "SUCCESS",
                          "message": "Configs Updated Successfully",
                          "configs": [
                            {
                              "id": 1,
                              "application": "user-service",
                              "profile": "prod",
                              "label": "v1.0.0",
                              "propKey": "database.url",
                              "propValue": "jdbc:postgresql://new-prod-db:5432/userdb"
                            },
                            {
                              "id": 2,
                              "application": "user-service",
                              "profile": "prod",
                              "label": "v1.0.0",
                              "propKey": "database.username",
                              "propValue": "newproduser"
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "One or more configurations not found",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Invalid request data",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    @PutMapping("/batch")
    public ResponseEntity<?> updateConfigs(
            @Parameter(description = "Batch configuration update request", required = true)
            @Valid @RequestBody ConfigListRequest request) {
        
        log.info("Updating {} configs in batch", request.getConfigs().size());
        
        try {
            ConfigListResponse response = configAPI.updateConfigs(request);
            log.info("Successfully updated {} configs in batch", response.getConfigs().size());
            return ResponseEntity.ok(response);
        } catch (ConfigAPIException e) {
            log.error("Error updating configs in batch: {}", e.getMessage());
            return ResponseEntity.status(getHttpStatus(e.getStatus()))
                    .body(new ErrorResponse(e.getStatus(), e.getMessage()));
        }
    }

    /**
     * Converts the internal configuration list response to Spring Cloud Config compatible format.
     * 
     * @param applicationName The application name
     * @param profile The environment profile
     * @param label The version label (can be null)
     * @param listResponse The internal configuration list response
     * @return ResponseEntity with Spring Cloud Config compatible format
     */
    private ResponseEntity<?> getResponseEntity(String applicationName, String profile, String label,
                                                ConfigListResponse listResponse) {
        Map<String, Object> response = new LinkedHashMap<>();
        
        // Add basic metadata
        if (!Strings.isEmpty(applicationName)) {
            response.put("name", applicationName);
        }
        if (!Strings.isEmpty(profile)) {
            response.put("profiles", new String[]{profile});
        }
        if (!Strings.isEmpty(label)) {
            response.put("label", label);
        }

        // Create property source
        Map<String, Object> source = new HashMap<>();
        for (Config config : listResponse.getConfigs()) {
            source.put(config.getPropKey(), config.getPropValue());
        }

        // Build property source name
        StringBuilder sourceNameBuilder = new StringBuilder();
        if (!Strings.isEmpty(applicationName)) {
            sourceNameBuilder.append(applicationName);
        }
        if (!Strings.isEmpty(profile)) {
            if (sourceNameBuilder.length() > 0) sourceNameBuilder.append("-");
            sourceNameBuilder.append(profile);
        }
        if (!Strings.isEmpty(label)) {
            if (sourceNameBuilder.length() > 0) sourceNameBuilder.append("-");
            sourceNameBuilder.append(label);
        }

        // Create property sources list
        List<PropertySource<?>> propertySources = new ArrayList<>();
        PropertySource<?> propertySource = new PropertySource<>(
                sourceNameBuilder.toString(),
                source
        ) {
            @Override
            public Object getProperty(String name) {
                return source.get(name);
            }
        };

        propertySources.add(propertySource);
        response.put("propertySources", propertySources);

        return ResponseEntity.ok(response);
    }

    /**
     * Maps internal status codes to HTTP status codes.
     * 
     * @param status The internal status code
     * @return The corresponding HTTP status
     */
    private HttpStatus getHttpStatus(String status) {
        return switch (status) {
            case "NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "CONFLICT" -> HttpStatus.CONFLICT;
            case "BAD_REQUEST" -> HttpStatus.BAD_REQUEST;
            case "VALIDATION_ERROR" -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    /**
     * Standard error response record for consistent error handling.
     * 
     * @param status The error status code
     * @param message The error message
     */
    @Schema(description = "Standard error response")
    public record ErrorResponse(
            @Schema(description = "Error status code", example = "NOT_FOUND")
            String status,
            
            @Schema(description = "Error message", example = "Configuration not found")
            String message
    ) {}
} 