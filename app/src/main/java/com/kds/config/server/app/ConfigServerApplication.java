package com.kds.config.server.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableConfigServer
@ComponentScan(basePackages = {"com.kds.config.server"})
@EntityScan(basePackages = {"com.kds.config.server.core.entity"})
@EnableJpaRepositories(basePackages = {"com.kds.config.server.core.repository"})
public class ConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args);
	}

}
