package com.kds.config.server.app.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration request DTO for creating and updating configuration properties.
 * 
 * @author KDS Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Configuration request for creating or updating a configuration property")
public class ConfigRequest {
    
    @NotBlank(message = "Application name cannot be blank")
    @Size(min = 1, max = 50, message = "Application name must be between 1 and 50 characters")
    @Pattern(regexp = "^[a-zA-Z0-9-_]+$", message = "Application name can only contain alphanumeric characters, hyphens, and underscores")
    @Schema(
        description = "Application name that this configuration belongs to",
        example = "user-service",
        required = true,
        maxLength = 50,
        pattern = "^[a-zA-Z0-9-_]+$"
    )
    private String application;
    
    @NotBlank(message = "Profile cannot be blank")
    @Size(min = 1, max = 20, message = "Profile must be between 1 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z0-9-_]+$", message = "Profile can only contain alphanumeric characters, hyphens, and underscores")
    @Schema(
        description = "Environment profile (e.g., dev, test, prod)",
        example = "prod",
        required = true,
        maxLength = 20,
        pattern = "^[a-zA-Z0-9-_]+$"
    )
    private String profile;
    
    @Size(max = 100, message = "Label cannot exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9.-_]*$", message = "Label can only contain alphanumeric characters, dots, hyphens, and underscores")
    @Schema(
        description = "Version label or branch name (optional)",
        example = "v1.0.0",
        maxLength = 100,
        pattern = "^[a-zA-Z0-9.-_]*$"
    )
    private String label;
    
    @NotBlank(message = "Configuration key cannot be blank")
    @Size(min = 1, max = 100, message = "Configuration key must be between 1 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Configuration key can only contain alphanumeric characters, dots, underscores, and hyphens")
    @Schema(
        description = "Configuration property key",
        example = "database.url",
        required = true,
        maxLength = 100,
        pattern = "^[a-zA-Z0-9._-]+$"
    )
    private String key;
    
    @NotBlank(message = "Configuration value cannot be blank")
    @Size(min = 1, max = 500, message = "Configuration value must be between 1 and 500 characters")
    @Schema(
        description = "Configuration property value",
        example = "jdbc:postgresql://prod-db:5432/userdb",
        required = true,
        maxLength = 500
    )
    private String value;
}