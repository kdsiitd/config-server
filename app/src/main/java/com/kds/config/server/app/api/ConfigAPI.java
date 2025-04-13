package com.kds.config.server.app.api;

import com.kds.config.server.app.dto.request.ConfigListRequest;
import com.kds.config.server.app.dto.request.ConfigRequest;
import com.kds.config.server.app.dto.response.ConfigListResponse;
import com.kds.config.server.app.dto.response.ConfigResponse;

public interface ConfigAPI {
    ConfigResponse getConfig(String application, String profile, String label, String key);
    
    ConfigListResponse getConfigs(String application, String profile, String label);
    
    ConfigListResponse getConfigs(String application, String profile);
    
    ConfigResponse saveConfig(ConfigRequest request);
    
    ConfigListResponse saveConfigs(ConfigListRequest request);
    
    void deleteConfig(String application, String profile, String label, String key);
}