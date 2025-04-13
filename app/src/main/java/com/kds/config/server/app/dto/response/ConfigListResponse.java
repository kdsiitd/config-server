package com.kds.config.server.app.dto.response;

import com.kds.config.server.core.entity.Config;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigListResponse {
    private String status;
    private String message;

    private List<Config> configs;
}