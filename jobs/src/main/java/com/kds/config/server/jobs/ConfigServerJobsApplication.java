package com.kds.config.server.jobs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ConfigServerJobsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerJobsApplication.class, args);
    }
} 