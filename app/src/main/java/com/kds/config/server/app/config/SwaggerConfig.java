package com.kds.config.server.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI configServerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Config Server API")
                        .description("RESTful API for managing application configurations")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("KDS")
                                .email("support@kds.com")
                                .url("https://kds.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
} 