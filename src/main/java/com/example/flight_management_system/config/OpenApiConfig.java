package com.example.flight_management_system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI flightManagementOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Flight Management API")
                .description("Partner-facing API for airlines, airports, flights, bookings, loyalty, and notifications")
                .version("v1")
                .contact(new Contact().name("Integration Team").email("integration@example.com"))
                .license(new License().name("Proprietary")));
    }
}
