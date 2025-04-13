package com.kds.config.server.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kds.config.server.core.entity.Config;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConfigRepository extends JpaRepository<Config, Long> {
    Optional<Config> findByApplicationAndProfileAndLabelAndPropKey(
            String application, String profile, String label, String propKey);
    
    List<Config> findByApplicationAndProfileAndLabel(
            String application, String profile, String label);
    
    List<Config> findByApplicationAndProfile(
            String application, String profile);

    List<Config> findByApplication(String application);

    void deleteByApplicationAndProfileAndLabelAndPropKey(String application, String profile, String label,
            String propKey);

    boolean existsByApplicationAndProfileAndLabelAndPropKey(String application, String profile, String label,
            String propKey);
} 