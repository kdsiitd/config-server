package com.kds.config.server.app.dto.response;

import com.kds.config.server.core.entity.Config;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigResponse {
    private String status;
    private String message;

    private Config config;
}