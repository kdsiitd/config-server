package com.kds.config.server.service;

import com.kds.config.server.core.entity.Config;
import com.kds.config.server.core.repository.ConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConfigServiceTests {

    @Mock
    private ConfigRepository configRepository;

    @InjectMocks
    private ConfigService configService;

    private Config testConfig;

    @BeforeEach
    void setUp() {
        testConfig = new Config();
        testConfig.setApplication("test-app");
        testConfig.setProfile("dev");
        testConfig.setLabel("main");
        testConfig.setPropKey("test.key");
        testConfig.setPropValue("test-value");
        testConfig.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        testConfig.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
    }

    @Test
    void whenGetConfigByKey_thenReturnConfig() {
        when(configRepository.findByApplicationAndProfileAndLabelAndPropKey(
                testConfig.getApplication(),
                testConfig.getProfile(),
                testConfig.getLabel(),
                testConfig.getPropKey()))
                .thenReturn(Optional.of(testConfig));

        Optional<Config> result = configService.getConfigByKey(
                testConfig.getApplication(),
                testConfig.getProfile(),
                testConfig.getLabel(),
                testConfig.getPropKey());

        assertThat(result).isPresent();
        assertThat(result.get().getPropValue()).isEqualTo(testConfig.getPropValue());
        verify(configRepository).findByApplicationAndProfileAndLabelAndPropKey(
                testConfig.getApplication(),
                testConfig.getProfile(),
                testConfig.getLabel(),
                testConfig.getPropKey());
    }

    @Test
    void whenGetConfigsByApplicationAndProfile_thenReturnConfigs() {
        when(configRepository.findByApplicationAndProfile(
                testConfig.getApplication(),
                testConfig.getProfile()))
                .thenReturn(List.of(testConfig));

        List<Config> results = configService.getConfigsByApplicationAndProfile(
                testConfig.getApplication(),
                testConfig.getProfile());

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getPropValue()).isEqualTo(testConfig.getPropValue());
        verify(configRepository).findByApplicationAndProfile(
                testConfig.getApplication(),
                testConfig.getProfile());
    }

    @Test
    void whenCreateConfig_thenReturnCreatedConfig() {
        when(configRepository.findByApplicationAndProfileAndLabelAndPropKey(
                testConfig.getApplication(),
                testConfig.getProfile(),
                testConfig.getLabel(),
                testConfig.getPropKey()))
                .thenReturn(Optional.empty());
        when(configRepository.save(any(Config.class))).thenReturn(testConfig);

        Config saved = configService.createConfig(testConfig);

        assertThat(saved).isNotNull();
        assertThat(saved.getPropValue()).isEqualTo(testConfig.getPropValue());
        verify(configRepository).save(testConfig);
    }

    @Test
    void whenCreateConfigWithExistingKey_thenThrowException() {
        when(configRepository.findByApplicationAndProfileAndLabelAndPropKey(
                testConfig.getApplication(),
                testConfig.getProfile(),
                testConfig.getLabel(),
                testConfig.getPropKey()))
                .thenReturn(Optional.of(testConfig));

        assertThat(catchThrowable(() -> configService.createConfig(testConfig)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Configuration already exists");
    }

    @Test
    void whenDeleteConfig_thenVerifyDeletion() {
        doNothing().when(configRepository).deleteByApplicationAndProfileAndLabelAndPropKey(
                testConfig.getApplication(),
                testConfig.getProfile(),
                testConfig.getLabel(),
                testConfig.getPropKey());

        configService.deleteConfig(
                testConfig.getApplication(),
                testConfig.getProfile(),
                testConfig.getLabel(),
                testConfig.getPropKey());

        verify(configRepository).deleteByApplicationAndProfileAndLabelAndPropKey(
                testConfig.getApplication(),
                testConfig.getProfile(),
                testConfig.getLabel(),
                testConfig.getPropKey());
    }
}
