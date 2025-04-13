package com.kds.config.server.core;

import com.kds.config.server.core.entity.Config;
import com.kds.config.server.core.repository.ConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ConfigRepositoryTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ConfigRepository configRepository;

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
        testConfig.setCreatedBy("test-user");
        testConfig.setUpdatedBy("test-user");
        
        entityManager.persist(testConfig);
        entityManager.flush();
    }

    @Test
    void whenFindByApplicationAndProfileAndLabel_thenReturnConfigs() {
        List<Config> found = configRepository.findByApplicationAndProfileAndLabel(
                testConfig.getApplication(),
                testConfig.getProfile(),
                testConfig.getLabel());

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getPropKey()).isEqualTo(testConfig.getPropKey());
        assertThat(found.get(0).getPropValue()).isEqualTo(testConfig.getPropValue());
    }

    @Test
    void whenFindByApplicationAndProfileAndLabelAndPropKey_thenReturnConfig() {
        Optional<Config> found = configRepository.findByApplicationAndProfileAndLabelAndPropKey(
                testConfig.getApplication(),
                testConfig.getProfile(),
                testConfig.getLabel(),
                testConfig.getPropKey());

        assertThat(found).isPresent();
        assertThat(found.get().getPropValue()).isEqualTo(testConfig.getPropValue());
    }

    @Test
    void whenFindByApplicationAndProfile_thenReturnConfigs() {
        List<Config> found = configRepository.findByApplicationAndProfile(
                testConfig.getApplication(),
                testConfig.getProfile());

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getPropKey()).isEqualTo(testConfig.getPropKey());
    }

    @Test
    void whenFindByApplication_thenReturnConfigs() {
        List<Config> found = configRepository.findByApplication(testConfig.getApplication());

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getPropKey()).isEqualTo(testConfig.getPropKey());
    }

    @Test
    void whenDeleteByApplicationAndProfileAndLabelAndPropKey_thenConfigDeleted() {
        configRepository.deleteByApplicationAndProfileAndLabelAndPropKey(
                testConfig.getApplication(),
                testConfig.getProfile(),
                testConfig.getLabel(),
                testConfig.getPropKey());

        Optional<Config> found = configRepository.findByApplicationAndProfileAndLabelAndPropKey(
                testConfig.getApplication(),
                testConfig.getProfile(),
                testConfig.getLabel(),
                testConfig.getPropKey());

        assertThat(found).isEmpty();
    }
}
