package com.kds.config.server.app.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigRequest {
    private String application;
    private String profile;
    private String label;
    private String key;
    private String value;
}