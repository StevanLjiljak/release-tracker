package com.releasetracker.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Release Tracker API",
                version = "1.0.0",
                description = "A production-ready REST API for tracking software releases through their lifecycle",
                contact = @Contact(name = "Release Tracker Team"),
                license = @License(name = "MIT")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local development")
        }
)
public class OpenApiConfig {
}
