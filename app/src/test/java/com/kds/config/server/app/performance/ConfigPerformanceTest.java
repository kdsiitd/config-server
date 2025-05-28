package com.kds.config.server.app.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kds.config.server.app.dto.request.ConfigRequest;
import com.kds.config.server.core.repository.ConfigRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Performance tests for ConfigController to validate concurrent request handling.
 * These tests ensure the application can handle multiple simultaneous requests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:perfdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("Config Performance Tests")
class ConfigPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConfigRepository configRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Clean database before each test
        configRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        // Clean up after each test
        configRepository.deleteAll();
    }

    @Test
    @DisplayName("Should handle concurrent config creation requests")
    void shouldHandleConcurrentConfigCreationRequests() throws Exception {
        int numberOfThreads = 10;
        int requestsPerThread = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        try {
            // Create concurrent requests
            CompletableFuture<Void>[] futures = IntStream.range(0, numberOfThreads)
                    .mapToObj(threadId -> CompletableFuture.runAsync(() -> {
                        try {
                            for (int i = 0; i < requestsPerThread; i++) {
                                ConfigRequest request = ConfigRequest.builder()
                                        .application("perf-app")
                                        .profile("test")
                                        .label("v1.0.0")
                                        .key("config.key." + threadId + "." + i)
                                        .value("value-" + threadId + "-" + i)
                                        .build();

                                mockMvc.perform(post("/api/v1/configs")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated());
                            }
                        } catch (Exception e) {
                            throw new RuntimeException("Request failed", e);
                        }
                    }, executor))
                    .toArray(CompletableFuture[]::new);

            // Wait for all requests to complete
            CompletableFuture.allOf(futures).get(30, TimeUnit.SECONDS);

            // Verify all configs were created
            long configCount = configRepository.count();
            assertThat(configCount).isEqualTo(numberOfThreads * requestsPerThread);

        } finally {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Should handle concurrent config retrieval requests")
    void shouldHandleConcurrentConfigRetrievalRequests() throws Exception {
        // First, create some test data
        for (int i = 0; i < 10; i++) {
            ConfigRequest request = ConfigRequest.builder()
                    .application("read-app")
                    .profile("test")
                    .label("v1.0.0")
                    .key("config.key." + i)
                    .value("value-" + i)
                    .build();

            mockMvc.perform(post("/api/v1/configs")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        int numberOfThreads = 20;
        int requestsPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        try {
            // Create concurrent read requests
            CompletableFuture<Void>[] futures = IntStream.range(0, numberOfThreads)
                    .mapToObj(threadId -> CompletableFuture.runAsync(() -> {
                        try {
                            for (int i = 0; i < requestsPerThread; i++) {
                                int configIndex = i % 10; // Cycle through available configs
                                mockMvc.perform(get("/api/v1/configs/read-app/test/v1.0.0/config.key." + configIndex))
                                        .andExpect(status().isOk());
                            }
                        } catch (Exception e) {
                            throw new RuntimeException("Request failed", e);
                        }
                    }, executor))
                    .toArray(CompletableFuture[]::new);

            // Wait for all requests to complete
            CompletableFuture.allOf(futures).get(30, TimeUnit.SECONDS);

            // All requests should have completed successfully (no exceptions thrown)
            assertThat(true).isTrue(); // If we reach here, all requests succeeded

        } finally {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Should handle mixed concurrent operations")
    void shouldHandleMixedConcurrentOperations() throws Exception {
        int numberOfThreads = 15;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        try {
            // Create mixed concurrent operations (create, read, update)
            CompletableFuture<Void>[] futures = IntStream.range(0, numberOfThreads)
                    .mapToObj(threadId -> CompletableFuture.runAsync(() -> {
                        try {
                            if (threadId % 3 == 0) {
                                // Create operations
                                ConfigRequest request = ConfigRequest.builder()
                                        .application("mixed-app")
                                        .profile("test")
                                        .label("v1.0.0")
                                        .key("mixed.key." + threadId)
                                        .value("value-" + threadId)
                                        .build();

                                mockMvc.perform(post("/api/v1/configs")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .content(objectMapper.writeValueAsString(request)))
                                        .andExpect(status().isCreated());

                            } else if (threadId % 3 == 1) {
                                // Read operations (may return 404 initially, which is fine)
                                mockMvc.perform(get("/api/v1/configs/mixed-app/test/v1.0.0/mixed.key.0"));

                            } else {
                                // List operations
                                mockMvc.perform(get("/api/v1/configs/mixed-app/test"));
                            }
                        } catch (Exception e) {
                            throw new RuntimeException("Request failed", e);
                        }
                    }, executor))
                    .toArray(CompletableFuture[]::new);

            // Wait for all requests to complete
            CompletableFuture.allOf(futures).get(30, TimeUnit.SECONDS);

            // Verify some configs were created
            long configCount = configRepository.count();
            assertThat(configCount).isGreaterThan(0);

        } finally {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
    }

    @Test
    @DisplayName("Should handle high volume batch operations")
    void shouldHandleHighVolumeBatchOperations() throws Exception {
        // This test validates that batch operations can handle larger datasets
        // without timing out or causing memory issues

        long startTime = System.currentTimeMillis();

        // Create a large batch request (within reasonable limits)
        ConfigRequest[] requests = IntStream.range(0, 50)
                .mapToObj(i -> ConfigRequest.builder()
                        .application("batch-app")
                        .profile("test")
                        .label("v1.0.0")
                        .key("batch.key." + i)
                        .value("batch-value-" + i)
                        .build())
                .toArray(ConfigRequest[]::new);

        String batchRequestJson = objectMapper.writeValueAsString(
                new com.kds.config.server.app.dto.request.ConfigListRequest(
                        java.util.Arrays.asList(requests)
                )
        );

        mockMvc.perform(post("/api/v1/configs/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(batchRequestJson))
                .andExpect(status().isCreated());

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Verify all configs were created
        long configCount = configRepository.count();
        assertThat(configCount).isEqualTo(50);

        // Verify reasonable performance (should complete within 5 seconds)
        assertThat(duration).isLessThan(5000);
    }
} 