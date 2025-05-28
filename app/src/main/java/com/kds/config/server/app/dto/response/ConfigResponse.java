package com.kds.config.server.app.dto.response;

import com.kds.config.server.core.entity.Config;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration response DTO containing the result of configuration operations.
 * 
 * @author KDS Team
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing a single configuration property and operation status")
public class ConfigResponse {
    
    @Schema(description = "Operation status", example = "SUCCESS")
    private String status;
    
    @Schema(description = "Operation message", example = "Config Retrieved")
    private String message;

    @Schema(description = "The configuration property", implementation = Config.class)
    private Config config;
}