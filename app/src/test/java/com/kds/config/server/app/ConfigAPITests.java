package com.kds.config.server.app;

import com.kds.config.server.app.api.impl.ConfigAPIImpl;
import com.kds.config.server.app.dto.request.ConfigListRequest;
import com.kds.config.server.app.dto.request.ConfigRequest;
import com.kds.config.server.app.dto.response.ConfigListResponse;
import com.kds.config.server.app.dto.response.ConfigResponse;
import com.kds.config.server.core.entity.Config;
import com.kds.config.server.service.ConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConfigAPITests {

    @Mock
    private ConfigService configService;

    @InjectMocks
    private ConfigAPIImpl configAPI;

    private Config testConfig;
    private ConfigRequest testRequest;
    private ConfigListRequest testListRequest;

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
        testConfig.setCreatedBy("test-user");
        testConfig.setUpdatedBy("test-user");

        testRequest = new ConfigRequest();
        testRequest.setApplication("test-app");
        testRequest.setProfile("dev");
        testRequest.setLabel("main");
        testRequest.setKey("test.key");
        testRequest.setValue("test-value");

        testListRequest = new ConfigListRequest();
        testListRequest.setConfigs(List.of(testRequest));
    }

    @Test
    void whenGetConfig_thenReturnConfig() {
        when(configService.getConfigByKey(
                testConfig.getApplication(),
                testConfig.getProfile(),
                testConfig.getLabel(),
                testConfig.getPropKey()))
                .thenReturn(Optional.of(testConfig));

        ConfigResponse response = configAPI.getConfig(
                testConfig.getApplication(),
                testConfig.getProfile(),
                testConfig.getLabel(),
                testConfig.getPropKey());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getMessage()).isEqualTo("Config Retrieved");
        assertThat(response.getConfig().getPropValue()).isEqualTo(testConfig.getPropValue());
    }

    @Test
    void whenGetConfigNotFound_thenThrowException() {
        when(configService.getConfigByKey(
                testConfig.getApplication(),
                testConfig.getProfile(),
                testConfig.getLabel(),
                testConfig.getPropKey()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> configAPI.getConfig(
                testConfig.getApplication(),
                testConfig.getProfile(),
                testConfig.getLabel(),
                testConfig.getPropKey()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Configuration not found");
    }

    @Test
    void whenGetConfigsByApplicationAndProfile_thenReturnConfigs() {
        when(configService.getConfigsByApplicationAndProfile(
                testConfig.getApplication(),
                testConfig.getProfile()))
                .thenReturn(List.of(testConfig));

        ConfigListResponse response = configAPI.getConfigs(
                testConfig.getApplication(),
                testConfig.getProfile());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getMessage()).isEqualTo("Retrieved Results");
        assertThat(response.getConfigs()).hasSize(1);
        assertThat(response.getConfigs().get(0).getPropValue()).isEqualTo(testConfig.getPropValue());
    }

    @Test
    void whenGetConfigsByApplicationAndProfileEmpty_thenReturnEmptyList() {
        when(configService.getConfigsByApplicationAndProfile(
                testConfig.getApplication(),
                testConfig.getProfile()))
                .thenReturn(Collections.emptyList());

        ConfigListResponse response = configAPI.getConfigs(
                testConfig.getApplication(),
                testConfig.getProfile());

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getMessage()).isEqualTo("Retrieved Results");
        assertThat(response.getConfigs()).isEmpty();
    }

    @Test
    void whenSaveConfig_thenReturnSavedConfig() {
        when(configService.createConfig(any(Config.class))).thenReturn(testConfig);

        ConfigResponse response = configAPI.saveConfig(testRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getMessage()).isEqualTo("Configs Saved");
        assertThat(response.getConfig().getPropValue()).isEqualTo(testConfig.getPropValue());
    }

    @Test
    void whenSaveConfigWithExistingKey_thenThrowException() {
        when(configService.createConfig(any(Config.class)))
                .thenThrow(new IllegalArgumentException("Configuration already exists"));

        assertThatThrownBy(() -> configAPI.saveConfig(testRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Configuration already exists");
    }

    @Test
    void whenSaveConfigs_thenReturnSavedConfigs() {
        when(configService.createConfig(any(Config.class))).thenReturn(testConfig);

        ConfigListResponse response = configAPI.saveConfigs(testListRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getMessage()).isEqualTo("Configs Saved Successfully");
        assertThat(response.getConfigs()).hasSize(1);
        assertThat(response.getConfigs().get(0).getPropValue()).isEqualTo(testConfig.getPropValue());
    }

    @Test
    void whenSaveConfigsWithEmptyList_thenReturnEmptyList() {
        testListRequest.setConfigs(Collections.emptyList());

        ConfigListResponse response = configAPI.saveConfigs(testListRequest);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("SUCCESS");
        assertThat(response.getMessage()).isEqualTo("Configs Saved Successfully");
        assertThat(response.getConfigs()).isEmpty();
    }

    @Test
    void whenSaveConfigsWithExistingKey_thenThrowException() {
        when(configService.createConfig(any(Config.class)))
                .thenThrow(new IllegalArgumentException("Configuration already exists"));

        assertThatThrownBy(() -> configAPI.saveConfigs(testListRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Configuration already exists");
    }

    @Test
    void whenDeleteConfig_thenNoException() {
        doNothing().when(configService).deleteConfig(
                testConfig.getApplication(),
                testConfig.getProfile(),
                testConfig.getLabel(),
                testConfig.getPropKey());

        configAPI.deleteConfig(
                testConfig.getApplication(),
                testConfig.getProfile(),
                testConfig.getLabel(),
                testConfig.getPropKey());

        verify(configService).deleteConfig(
                testConfig.getApplication(),
                testConfig.getProfile(),
                testConfig.getLabel(),
                testConfig.getPropKey());
    }
}
