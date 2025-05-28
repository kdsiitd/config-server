package com.kds.config.server.app.dto.response;

import com.kds.config.server.core.entity.Config;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Configuration list response DTO containing the result of batch configuration operations.
 * 
 * @author KDS Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing multiple configuration properties and operation status")
public class ConfigListResponse {
    
    @Schema(description = "Operation status", example = "SUCCESS")
    private String status;
    
    @Schema(description = "Operation message", example = "Configs Retrieved")
    private String message;

    @ArraySchema(
        arraySchema = @Schema(description = "List of configuration properties"),
        schema = @Schema(implementation = Config.class)
    )
    private List<Config> configs;
}