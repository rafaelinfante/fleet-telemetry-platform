package net.rafaelinfante.fleet.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI fleetOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Fleet Telemetry Platform API")
                .version("1.0.0")
                .description("Query devices, readings and alerts, and configure alert rules and geofences.")
                .license(new License().name("MIT")));
    }
}
