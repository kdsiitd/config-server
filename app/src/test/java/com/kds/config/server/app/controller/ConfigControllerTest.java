package com.kds.config.server.app.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kds.config.server.app.api.ConfigAPI;
import com.kds.config.server.app.dto.request.ConfigRequest;
import com.kds.config.server.app.dto.response.ConfigListResponse;
import com.kds.config.server.app.dto.response.ConfigResponse;
import com.kds.config.server.app.exception.ConfigAPIException;
import com.kds.config.server.core.entity.Config;

/**
 * Comprehensive unit tests for ConfigController.
 * Tests all endpoints, validation, error handling, and edge cases.
 */
@Tag("skip")
@WebMvcTest(ConfigController.class)
@ActiveProfiles("test")
@DisplayName("ConfigController Tests")
class ConfigControllerTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ConfigAPI configAPI;

    private ConfigRequest validConfigRequest;
    private Config validConfig;
    private ConfigResponse validResponse;
    private ConfigListResponse validConfigsResponse;

    @BeforeEach
    void setUp() {
        // Setup valid ConfigRequest
        validConfigRequest = ConfigRequest.builder()
                .application("test-app")
                .profile("dev")
                .label("latest")
                .key("test.key")
                .value("test-value")
                .build();

        // Setup valid Config entity
        validConfig = Config.builder()
                .application("test-app")
                .profile("dev")
                .label("latest")
                .propKey("test.key")
                .propValue("test-value")
                .build();

        // Setup valid response DTOs
        validResponse = ConfigResponse.builder()
                .status("SUCCESS")
                .message("Config Retrieved")
                .config(validConfig)
                .build();

        validConfigsResponse = ConfigListResponse.builder()
                .status("SUCCESS")
                .message("Configs Retrieved")
                .configs(Collections.singletonList(validConfig))
                .build();
    }

    @Nested
    @DisplayName("GET /api/v1/configs/{application}/{profile}/{label}/{key}")
    class GetConfigTests {
        @Test
        @DisplayName("Should return config when found")
        void shouldReturnConfigWhenFound() throws Exception {
            when(configAPI.getConfig("test-app", "dev", "latest", "test.key"))
                    .thenReturn(validResponse);

            mockMvc.perform(get("/api/v1/configs/test-app/dev/latest/test.key"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("Config Retrieved"))
                    .andExpect(jsonPath("$.config.application").value("test-app"))
                    .andExpect(jsonPath("$.config.profile").value("dev"))
                    .andExpect(jsonPath("$.config.label").value("latest"))
                    .andExpect(jsonPath("$.config.propKey").value("test.key"))
                    .andExpect(jsonPath("$.config.propValue").value("test-value"));
        }

        @Test
        @DisplayName("Should return 404 when config not found")
        void shouldReturn404WhenConfigNotFound() throws Exception {
            when(configAPI.getConfig(anyString(), anyString(), anyString(), anyString()))
                    .thenThrow(new ConfigAPIException("NOT_FOUND", "Configuration not found"));

            mockMvc.perform(get("/api/v1/configs/test-app/dev/latest/nonexistent.key"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("Configuration not found"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/configs/{application}/{profile}/{label}")
    class GetConfigsWithLabelTests {
        @Test
        @DisplayName("Should return configs when found")
        void shouldReturnConfigsWhenFound() throws Exception {
            when(configAPI.getConfigs("test-app", "dev", "latest"))
                    .thenReturn(validConfigsResponse);

            mockMvc.perform(get("/api/v1/configs/test-app/dev/latest"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("Configs Retrieved"))
                    .andExpect(jsonPath("$.configs", hasSize(1)))
                    .andExpect(jsonPath("$.configs[0].application").value("test-app"))
                    .andExpect(jsonPath("$.configs[0].profile").value("dev"))
                    .andExpect(jsonPath("$.configs[0].label").value("latest"))
                    .andExpect(jsonPath("$.configs[0].propKey").value("test.key"))
                    .andExpect(jsonPath("$.configs[0].propValue").value("test-value"));
        }

        @Test
        @DisplayName("Should return empty list when no configs found")
        void shouldReturnEmptyListWhenNoConfigsFound() throws Exception {
            when(configAPI.getConfigs(anyString(), anyString(), anyString()))
                    .thenReturn(ConfigListResponse.builder().status("SUCCESS").message("Configs Retrieved").configs(Collections.emptyList()).build());

            mockMvc.perform(get("/api/v1/configs/test-app/dev/latest"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("Configs Retrieved"))
                    .andExpect(jsonPath("$.configs", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/configs/{application}/{profile}")
    class GetConfigsWithoutLabelTests {
        @Test
        @DisplayName("Should return configs when found")
        void shouldReturnConfigsWhenFound() throws Exception {
            when(configAPI.getConfigs("test-app", "dev"))
                    .thenReturn(validConfigsResponse);

            mockMvc.perform(get("/api/v1/configs/test-app/dev"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("Configs Retrieved"))
                    .andExpect(jsonPath("$.configs", hasSize(1)))
                    .andExpect(jsonPath("$.configs[0].application").value("test-app"))
                    .andExpect(jsonPath("$.configs[0].profile").value("dev"))
                    .andExpect(jsonPath("$.configs[0].label").value("latest"))
                    .andExpect(jsonPath("$.configs[0].propKey").value("test.key"))
                    .andExpect(jsonPath("$.configs[0].propValue").value("test-value"));
        }

        @Test
        @DisplayName("Should return empty list when no configs found")
        void shouldReturnEmptyListWhenNoConfigsFound() throws Exception {
            when(configAPI.getConfigs(anyString(), anyString()))
                    .thenReturn(ConfigListResponse.builder().status("SUCCESS").message("Configs Retrieved").configs(Collections.emptyList()).build());

            mockMvc.perform(get("/api/v1/configs/test-app/dev"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("Configs Retrieved"))
                    .andExpect(jsonPath("$.configs", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/configs")
    class CreateConfigTests {
        @Test
        @DisplayName("Should create config successfully")
        void shouldCreateConfigSuccessfully() throws Exception {
            // Update the response to match what the controller actually returns
            ConfigResponse createResponse = ConfigResponse.builder()
                    .status("SUCCESS")
                    .message("Config Saved")
                    .config(validConfig)
                    .build();
            
            when(configAPI.saveConfig(any(ConfigRequest.class)))
                    .thenReturn(createResponse);

            mockMvc.perform(post("/api/v1/configs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validConfigRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("Config Saved"))
                    .andExpect(jsonPath("$.config.application").value("test-app"))
                    .andExpect(jsonPath("$.config.profile").value("dev"))
                    .andExpect(jsonPath("$.config.label").value("latest"))
                    .andExpect(jsonPath("$.config.propKey").value("test.key"))
                    .andExpect(jsonPath("$.config.propValue").value("test-value"));
        }

        @Test
        @DisplayName("Should return 409 when config already exists")
        void shouldReturn409WhenConfigAlreadyExists() throws Exception {
            when(configAPI.saveConfig(any(ConfigRequest.class)))
                    .thenThrow(new ConfigAPIException("CONFLICT", "Configuration already exists"));

            mockMvc.perform(post("/api/v1/configs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validConfigRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value("CONFLICT"))
                    .andExpect(jsonPath("$.message").value("Configuration already exists"));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/configs")
    class UpdateConfigTests {
        @Test
        @DisplayName("Should update config successfully")
        void shouldUpdateConfigSuccessfully() throws Exception {
            // Update the response to match what the controller actually returns
            ConfigResponse updateResponse = ConfigResponse.builder()
                    .status("SUCCESS")
                    .message("Config Updated")
                    .config(validConfig)
                    .build();
            
            when(configAPI.updateConfig(any(ConfigRequest.class)))
                    .thenReturn(updateResponse);

            mockMvc.perform(put("/api/v1/configs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validConfigRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("Config Updated"))
                    .andExpect(jsonPath("$.config.application").value("test-app"))
                    .andExpect(jsonPath("$.config.profile").value("dev"))
                    .andExpect(jsonPath("$.config.label").value("latest"))
                    .andExpect(jsonPath("$.config.propKey").value("test.key"))
                    .andExpect(jsonPath("$.config.propValue").value("test-value"));
        }

        @Test
        @DisplayName("Should return 404 when config not found")
        void shouldReturn404WhenConfigNotFound() throws Exception {
            when(configAPI.updateConfig(any(ConfigRequest.class)))
                    .thenThrow(new ConfigAPIException("NOT_FOUND", "Configuration not found"));

            mockMvc.perform(put("/api/v1/configs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validConfigRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("Configuration not found"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/configs/{application}/{profile}/{label}/{key}")
    class DeleteConfigTests {
        @Test
        @DisplayName("Should delete config successfully")
        void shouldDeleteConfigSuccessfully() throws Exception {
            doNothing().when(configAPI).deleteConfig("test-app", "dev", "latest", "test.key");

            mockMvc.perform(delete("/api/v1/configs/test-app/dev/latest/test.key"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("Should return 404 when config not found")
        void shouldReturn404WhenConfigNotFound() throws Exception {
            doThrow(new ConfigAPIException("NOT_FOUND", "Configuration not found"))
                    .when(configAPI).deleteConfig(anyString(), anyString(), anyString(), anyString());

            mockMvc.perform(delete("/api/v1/configs/test-app/dev/latest/nonexistent.key"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                    .andExpect(jsonPath("$.message").value("Configuration not found"));
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandlingTests {
        @Test
        @DisplayName("Should handle invalid request body")
        void shouldHandleInvalidRequestBody() throws Exception {
            mockMvc.perform(post("/api/v1/configs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("invalid json"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle missing content type")
        void shouldHandleMissingContentType() throws Exception {
            mockMvc.perform(post("/api/v1/configs")
                            .content(objectMapper.writeValueAsString(validConfigRequest)))
                    .andExpect(status().isUnsupportedMediaType());
        }

        @Test
        @DisplayName("Should handle validation errors")
        void shouldHandleValidationErrors() throws Exception {
            ConfigRequest invalidRequest = ConfigRequest.builder()
                    .application("")  // Invalid: empty application
                    .profile("dev")
                    .label("latest")
                    .key("test.key")
                    .value("test-value")
                    .build();

            mockMvc.perform(post("/api/v1/configs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }
} 