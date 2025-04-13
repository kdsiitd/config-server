package com.kds.config.server.service;

import com.kds.config.server.core.entity.Config;
import com.kds.config.server.core.repository.ConfigRepository;
import com.kds.config.server.service.exception.ConfigServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ConfigService {
    private final ConfigRepository configRepository;

    public ConfigService(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Transactional(readOnly = true)
    public List<Config> getAllConfigs() {
        return configRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Config> getConfigsByApplication(String application) {
        return configRepository.findByApplication(application);
    }

    @Transactional(readOnly = true)
    public List<Config> getConfigsByApplicationAndProfile(String application, String profile) {
        return configRepository.findByApplicationAndProfile(application, profile);
    }

    @Transactional(readOnly = true)
    public List<Config> getConfigsByApplicationAndProfileAndLabel(String application, String profile, String label) {
        return configRepository.findByApplicationAndProfileAndLabel(application, profile, label);
    }

    @Transactional(readOnly = true)
    public Optional<Config> getConfigByKey(String application, String profile, String label, String key) {
        return configRepository.findByApplicationAndProfileAndLabelAndPropKey(application, profile, label, key);
    }

    @Transactional
    public Config createConfig(Config config) {
        if (configRepository.findByApplicationAndProfileAndLabelAndPropKey(
                config.getApplication(), config.getProfile(), config.getLabel(), config.getPropKey()).isPresent()) {
            throw new ConfigServiceException("CONFLICT", "Configuration already exists");
        }
        return configRepository.save(config);
    }

    @Transactional
    public Config updateConfig(Config config) {
        return configRepository.findByApplicationAndProfileAndLabelAndPropKey(
                config.getApplication(), config.getProfile(), config.getLabel(), config.getPropKey())
                .map(existingConfig -> {
                    existingConfig.setPropValue(config.getPropValue());
                    return configRepository.save(existingConfig);
                })
                .orElseThrow(() -> new ConfigServiceException("NOT_FOUND", "Configuration not found"));
    }

    @Transactional
    public void deleteConfig(String application, String profile, String label, String key) {
        if (!configRepository.existsByApplicationAndProfileAndLabelAndPropKey(application, profile, label, key)) {
            throw new ConfigServiceException("NOT_FOUND", "Configuration not found");
        }
        configRepository.deleteByApplicationAndProfileAndLabelAndPropKey(application, profile, label, key);
    }
}
