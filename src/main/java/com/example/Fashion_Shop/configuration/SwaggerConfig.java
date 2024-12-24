package com.example.Fashion_Shop.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "Fashion-shop ASM API in Java Spring boot",
                version = "1.0.0",
                description = "Website bán quần áo ASM"
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local Development Server"),
        }
)

//@SecurityScheme(
//        name = "bearer-key", // Can be any name, used to reference this scheme in the @SecurityRequirement annotation
//        type = SecuritySchemeType.HTTP,
//        scheme = "bearer",
//        bearerFormat = "JWT",
//        in = SecuritySchemeIn.HEADER
//)
@Configuration
public class SwaggerConfig {
}
