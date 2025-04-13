package com.kds.config.server.service;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = {
    "com.kds.config.server.service",
    "com.kds.config.server.core"
})
@EntityScan(basePackages = "com.kds.config.server.core.entity")
@EnableJpaRepositories(basePackages = "com.kds.config.server.core.repository")
public class TestConfig {
} 