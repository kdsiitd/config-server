package com.kds.config.server.app.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kds.config.server.app.dto.request.ConfigListRequest;
import com.kds.config.server.app.dto.request.ConfigRequest;
import com.kds.config.server.core.entity.Config;
import com.kds.config.server.core.repository.ConfigRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ConfigController that test the full application stack.
 * Uses H2 in-memory database for testing.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("ConfigController Integration Tests")
class ConfigControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private ConfigRequest testRequest;
    private ConfigListRequest testListRequest;

    @BeforeEach
    void setUp() {
        testRequest = ConfigRequest.builder()
                .application("integration-test-app")
                .profile("test")
                .label("v1.0.0")
                .key("test.property")
                .value("test-value")
                .build();

        ConfigRequest secondRequest = ConfigRequest.builder()
                .application("integration-test-app")
                .profile("test")
                .label("v1.0.0")
                .key("test.property2")
                .value("test-value2")
                .build();

        testListRequest = ConfigListRequest.builder()
                .configs(Arrays.asList(testRequest, secondRequest))
                .build();
    }

    @AfterEach
    void tearDown() {
        // Clean up test data
        configRepository.deleteAll();
    }

    @Nested
    @DisplayName("Configuration CRUD Operations")
    class CrudOperationsTests {

        @Test
        @Order(1)
        @DisplayName("Should create configuration successfully")
        void shouldCreateConfigurationSuccessfully() throws Exception {
            mockMvc.perform(post("/api/v1/configs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("Configs Saved"))
                    .andExpect(jsonPath("$.config.application").value("integration-test-app"))
                    .andExpect(jsonPath("$.config.propKey").value("test.property"))
                    .andExpect(jsonPath("$.config.propValue").value("test-value"));

            // Verify in database
            Optional<Config> savedConfig = configRepository.findByApplicationAndProfileAndLabelAndPropKey(
                    "integration-test-app", "test", "v1.0.0", "test.property");
            assertThat(savedConfig).isPresent();
            assertThat(savedConfig.get().getPropValue()).isEqualTo("test-value");
        }

        @Test
        @Order(2)
        @DisplayName("Should retrieve configuration successfully")
        void shouldRetrieveConfigurationSuccessfully() throws Exception {
            // First create a config
            Config config = Config.builder()
                    .application("integration-test-app")
                    .profile("test")
                    .label("v1.0.0")
                    .propKey("test.property")
                    .propValue("test-value")
                    .build();
            configRepository.save(config);

            mockMvc.perform(get("/api/v1/configs/integration-test-app/test/v1.0.0/test.property"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.config.propValue").value("test-value"));
        }

        @Test
        @Order(3)
        @DisplayName("Should update configuration successfully")
        void shouldUpdateConfigurationSuccessfully() throws Exception {
            // First create a config
            Config config = Config.builder()
                    .application("integration-test-app")
                    .profile("test")
                    .label("v1.0.0")
                    .propKey("test.property")
                    .propValue("original-value")
                    .build();
            configRepository.save(config);

            // Update the config
            ConfigRequest updateRequest = ConfigRequest.builder()
                    .application("integration-test-app")
                    .profile("test")
                    .label("v1.0.0")
                    .key("test.property")
                    .value("updated-value")
                    .build();

            mockMvc.perform(put("/api/v1/configs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.message").value("Config Updated"))
                    .andExpect(jsonPath("$.config.propValue").value("updated-value"));

            // Verify in database
            Optional<Config> updatedConfig = configRepository.findByApplicationAndProfileAndLabelAndPropKey(
                    "integration-test-app", "test", "v1.0.0", "test.property");
            assertThat(updatedConfig).isPresent();
            assertThat(updatedConfig.get().getPropValue()).isEqualTo("updated-value");
        }

        @Test
        @Order(4)
        @DisplayName("Should delete configuration successfully")
        void shouldDeleteConfigurationSuccessfully() throws Exception {
            // First create a config
            Config config = Config.builder()
                    .application("integration-test-app")
                    .profile("test")
                    .label("v1.0.0")
                    .propKey("test.property")
                    .propValue("test-value")
                    .build();
            configRepository.save(config);

            mockMvc.perform(delete("/api/v1/configs/integration-test-app/test/v1.0.0/test.property"))
                    .andDo(print())
                    .andExpect(status().isNoContent());

            // Verify deletion in database
            Optional<Config> deletedConfig = configRepository.findByApplicationAndProfileAndLabelAndPropKey(
                    "integration-test-app", "test", "v1.0.0", "test.property");
            assertThat(deletedConfig).isEmpty();
        }
    }

    @Nested
    @DisplayName("Batch Operations")
    class BatchOperationsTests {

        @Test
        @DisplayName("Should create multiple configurations in batch")
        @Transactional
        void shouldCreateMultipleConfigurationsInBatch() throws Exception {
            mockMvc.perform(post("/api/v1/configs/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(testListRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.configs").isArray())
                    .andExpect(jsonPath("$.configs.length()").value(2));

            // Verify in database
            List<Config> savedConfigs = configRepository.findByApplicationAndProfile(
                    "integration-test-app", "test");
            assertThat(savedConfigs).hasSize(2);
        }

        @Test
        @DisplayName("Should update multiple configurations in batch")
        @Transactional
        void shouldUpdateMultipleConfigurationsInBatch() throws Exception {
            // First create configs
            Config config1 = Config.builder()
                    .application("integration-test-app")
                    .profile("test")
                    .label("v1.0.0")
                    .propKey("test.property")
                    .propValue("original-value1")
                    .build();

            Config config2 = Config.builder()
                    .application("integration-test-app")
                    .profile("test")
                    .label("v1.0.0")
                    .propKey("test.property2")
                    .propValue("original-value2")
                    .build();

            configRepository.saveAll(Arrays.asList(config1, config2));

            // Update configs
            ConfigRequest updateRequest1 = ConfigRequest.builder()
                    .application("integration-test-app")
                    .profile("test")
                    .label("v1.0.0")
                    .key("test.property")
                    .value("updated-value1")
                    .build();

            ConfigRequest updateRequest2 = ConfigRequest.builder()
                    .application("integration-test-app")
                    .profile("test")
                    .label("v1.0.0")
                    .key("test.property2")
                    .value("updated-value2")
                    .build();

            ConfigListRequest updateListRequest = ConfigListRequest.builder()
                    .configs(Arrays.asList(updateRequest1, updateRequest2))
                    .build();

            mockMvc.perform(put("/api/v1/configs/batch")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateListRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.configs.length()").value(2));

            // Verify updates in database
            Optional<Config> updatedConfig1 = configRepository.findByApplicationAndProfileAndLabelAndPropKey(
                    "integration-test-app", "test", "v1.0.0", "test.property");
            Optional<Config> updatedConfig2 = configRepository.findByApplicationAndProfileAndLabelAndPropKey(
                    "integration-test-app", "test", "v1.0.0", "test.property2");

            assertThat(updatedConfig1).isPresent();
            assertThat(updatedConfig1.get().getPropValue()).isEqualTo("updated-value1");
            assertThat(updatedConfig2).isPresent();
            assertThat(updatedConfig2.get().getPropValue()).isEqualTo("updated-value2");
        }
    }

    @Nested
    @DisplayName("Spring Cloud Config Format")
    class SpringCloudConfigFormatTests {

        @Test
        @DisplayName("Should return configs in Spring Cloud Config format")
        void shouldReturnConfigsInSpringCloudConfigFormat() throws Exception {
            // Create test configs
            Config config1 = Config.builder()
                    .application("spring-app")
                    .profile("prod")
                    .label("v2.0.0")
                    .propKey("database.url")
                    .propValue("jdbc:postgresql://prod-db:5432/springapp")
                    .build();

            Config config2 = Config.builder()
                    .application("spring-app")
                    .profile("prod")
                    .label("v2.0.0")
                    .propKey("database.username")
                    .propValue("produser")
                    .build();

            configRepository.saveAll(Arrays.asList(config1, config2));

            mockMvc.perform(get("/api/v1/configs/spring-app/prod/v2.0.0"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("spring-app"))
                    .andExpect(jsonPath("$.profiles[0]").value("prod"))
                    .andExpect(jsonPath("$.label").value("v2.0.0"))
                    .andExpect(jsonPath("$.propertySources").isArray())
                    .andExpect(jsonPath("$.propertySources[0].name").value("spring-app-prod-v2.0.0"))
                    .andExpect(jsonPath("$.propertySources[0].source['database.url']")
                            .value("jdbc:postgresql://prod-db:5432/springapp"))
                    .andExpect(jsonPath("$.propertySources[0].source['database.username']")
                            .value("produser"));
        }

        @Test
        @DisplayName("Should return configs without label in Spring Cloud Config format")
        void shouldReturnConfigsWithoutLabelInSpringCloudConfigFormat() throws Exception {
            // Create test config without specific label
            Config config = Config.builder()
                    .application("spring-app")
                    .profile("dev")
                    .label("main")
                    .propKey("debug.enabled")
                    .propValue("true")
                    .build();

            configRepository.save(config);

            mockMvc.perform(get("/api/v1/configs/spring-app/dev"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("spring-app"))
                    .andExpect(jsonPath("$.profiles[0]").value("dev"))
                    .andExpect(jsonPath("$.label").doesNotExist())
                    .andExpect(jsonPath("$.propertySources[0].source['debug.enabled']").value("true"));
        }
    }

    @Nested
    @DisplayName("Error Scenarios")
    class ErrorScenariosTests {

        @Test
        @DisplayName("Should return 404 for non-existent configuration")
        void shouldReturn404ForNonExistentConfiguration() throws Exception {
            mockMvc.perform(get("/api/v1/configs/non-existent-app/prod/v1.0.0/non.existent.key"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("NOT_FOUND"));
        }

        @Test
        @DisplayName("Should return 409 for duplicate configuration creation")
        void shouldReturn409ForDuplicateConfigurationCreation() throws Exception {
            // First create a config
            Config existingConfig = Config.builder()
                    .application("duplicate-app")
                    .profile("test")
                    .label("v1.0.0")
                    .propKey("duplicate.key")
                    .propValue("existing-value")
                    .build();
            configRepository.save(existingConfig);

            // Try to create the same config again
            ConfigRequest duplicateRequest = ConfigRequest.builder()
                    .application("duplicate-app")
                    .profile("test")
                    .label("v1.0.0")
                    .key("duplicate.key")
                    .value("new-value")
                    .build();

            mockMvc.perform(post("/api/v1/configs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(duplicateRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value("CONFLICT"));
        }

        @Test
        @DisplayName("Should return 404 for updating non-existent configuration")
        void shouldReturn404ForUpdatingNonExistentConfiguration() throws Exception {
            ConfigRequest updateRequest = ConfigRequest.builder()
                    .application("non-existent-app")
                    .profile("test")
                    .label("v1.0.0")
                    .key("non.existent.key")
                    .value("new-value")
                    .build();

            mockMvc.perform(put("/api/v1/configs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("NOT_FOUND"));
        }

        @Test
        @DisplayName("Should return 404 for deleting non-existent configuration")
        void shouldReturn404ForDeletingNonExistentConfiguration() throws Exception {
            mockMvc.perform(delete("/api/v1/configs/non-existent-app/test/v1.0.0/non.existent.key"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("NOT_FOUND"));
        }
    }

    @Nested
    @DisplayName("Data Validation")
    class DataValidationTests {

        @Test
        @DisplayName("Should validate request data and return 400 for invalid input")
        void shouldValidateRequestDataAndReturn400ForInvalidInput() throws Exception {
            ConfigRequest invalidRequest = ConfigRequest.builder()
                    .application("") // Invalid: empty
                    .profile("test")
                    .label("v1.0.0")
                    .key("test.key")
                    .value("test-value")
                    .build();

            mockMvc.perform(post("/api/v1/configs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should validate pattern constraints")
        void shouldValidatePatternConstraints() throws Exception {
            ConfigRequest invalidPatternRequest = ConfigRequest.builder()
                    .application("invalid@app") // Invalid: contains @
                    .profile("test")
                    .label("v1.0.0")
                    .key("test.key")
                    .value("test-value")
                    .build();

            mockMvc.perform(post("/api/v1/configs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidPatternRequest)))
                    .andExpect(status().isBadRequest());
        }
    }
} 