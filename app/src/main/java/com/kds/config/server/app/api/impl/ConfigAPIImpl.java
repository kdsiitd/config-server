package com.kds.config.server.app.api.impl;

import com.kds.config.server.app.api.ConfigAPI;
import com.kds.config.server.app.dto.request.ConfigListRequest;
import com.kds.config.server.app.dto.request.ConfigRequest;
import com.kds.config.server.app.dto.response.ConfigListResponse;
import com.kds.config.server.app.dto.response.ConfigResponse;
import com.kds.config.server.app.exception.ConfigAPIException;
import com.kds.config.server.core.entity.Config;
import com.kds.config.server.service.ConfigService;
import com.kds.config.server.service.exception.ConfigServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ConfigAPIImpl implements ConfigAPI {
    private final ConfigService configService;

    public ConfigAPIImpl(ConfigService configService) {
        this.configService = configService;
    }

    @Override
    public ConfigResponse getConfig(String application, String profile, String label, String key) {
        try {
            Config config = configService.getConfigByKey(application, profile, label, key)
                    .orElseThrow(() -> new ConfigAPIException("NOT_FOUND", "Configuration not found"));

            return ConfigResponse.builder()
                    .status("SUCCESS")
                    .message("Config Retrieved")
                    .config(config)
                    .build();
        } catch (ConfigServiceException e) {
            throw new ConfigAPIException(e.getStatus(), e.getMessage());
        }
    }

    @Override
    public ConfigListResponse getConfigs(String application, String profile, String label) {
        try {
            List<Config> configs = configService.getConfigsByApplicationAndProfileAndLabel(application, profile, label);

            return ConfigListResponse.builder()
                    .status("SUCCESS")
                    .message("Retrieved Results")
                    .configs(configs)
                    .build();
        } catch (ConfigServiceException e) {
            throw new ConfigAPIException(e.getStatus(), e.getMessage());
        }
    }

    @Override
    public ConfigListResponse getConfigs(String application, String profile) {
        try {
            List<Config> configs = configService.getConfigsByApplicationAndProfile(application, profile);

            return ConfigListResponse.builder()
                    .status("SUCCESS")
                    .message("Retrieved Results")
                    .configs(configs)
                    .build();
        } catch (ConfigServiceException e) {
            throw new ConfigAPIException(e.getStatus(), e.getMessage());
        }
    }

    @Override
    public ConfigResponse saveConfig(ConfigRequest request) {
        try {
            Config config = Config.builder()
                    .application(request.getApplication())
                    .profile(request.getProfile())
                    .label(request.getLabel())
                    .propKey(request.getKey())
                    .propValue(request.getValue())
                    .build();
            
            Config dbConfig = configService.createConfig(config);

            return ConfigResponse.builder()
                    .status("SUCCESS")
                    .message("Configs Saved")
                    .config(dbConfig)
                    .build();
        } catch (ConfigServiceException e) {
            throw new ConfigAPIException(e.getStatus(), e.getMessage());
        }
    }

    @Override
    @Transactional
    public ConfigListResponse saveConfigs(ConfigListRequest request) {
        try {
            List<Config> configs = request.getConfigs().stream()
                    .map(req -> Config.builder()
                            .application(req.getApplication())
                            .profile(req.getProfile())
                            .label(req.getLabel())
                            .propKey(req.getKey())
                            .propValue(req.getValue())
                            .build())
                    .collect(Collectors.toList());

            List<Config> savedConfigs = configs.stream()
                    .map(configService::createConfig)
                    .collect(Collectors.toList());

            return ConfigListResponse.builder()
                    .status("SUCCESS")
                    .message("Configs Saved Successfully")
                    .configs(savedConfigs)
                    .build();
        } catch (ConfigServiceException e) {
            throw new ConfigAPIException(e.getStatus(), e.getMessage());
        }
    }

    @Override
    public void deleteConfig(String application, String profile, String label, String key) {
        try {
            configService.deleteConfig(application, profile, label, key);
        } catch (ConfigServiceException e) {
            throw new ConfigAPIException(e.getStatus(), e.getMessage());
        }
    }

    @Override
    public ConfigResponse updateConfig(ConfigRequest request) {
        try {
            Config config = Config.builder()
                    .application(request.getApplication())
                    .profile(request.getProfile())
                    .label(request.getLabel())
                    .propKey(request.getKey())
                    .propValue(request.getValue())
                    .build();
            
            Config updatedConfig = configService.updateConfig(config);

            return ConfigResponse.builder()
                    .status("SUCCESS")
                    .message("Config Updated")
                    .config(updatedConfig)
                    .build();
        } catch (ConfigServiceException e) {
            throw new ConfigAPIException(e.getStatus(), e.getMessage());
        }
    }

    @Override
    @Transactional
    public ConfigListResponse updateConfigs(ConfigListRequest request) {
        try {
            List<Config> configs = request.getConfigs().stream()
                    .map(req -> Config.builder()
                            .application(req.getApplication())
                            .profile(req.getProfile())
                            .label(req.getLabel())
                            .propKey(req.getKey())
                            .propValue(req.getValue())
                            .build())
                    .collect(Collectors.toList());

            List<Config> updatedConfigs = configs.stream()
                    .map(configService::updateConfig)
                    .collect(Collectors.toList());

            return ConfigListResponse.builder()
                    .status("SUCCESS")
                    .message("Configs Updated Successfully")
                    .configs(updatedConfigs)
                    .build();
        } catch (ConfigServiceException e) {
            throw new ConfigAPIException(e.getStatus(), e.getMessage());
        }
    }
}