package com.kds.config.server.app.dto.request;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Configuration list request DTO for batch operations on configuration properties.
 * 
 * @author KDS Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Batch configuration request for creating or updating multiple configuration properties")
public class ConfigListRequest {
    
    @NotEmpty(message = "Configuration list cannot be empty")
    @Size(min = 1, max = 100, message = "Configuration list must contain between 1 and 100 items")
    @Valid
    @ArraySchema(
        arraySchema = @Schema(description = "List of configuration requests"),
        schema = @Schema(implementation = ConfigRequest.class)
    )
    private List<ConfigRequest> configs;
} 