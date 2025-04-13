package com.kds.config.server.core;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = "com.kds.config.server.core.entity")
@EnableJpaRepositories(basePackages = "com.kds.config.server.core.repository")
public class TestConfig {
} 