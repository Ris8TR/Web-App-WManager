package com.myTesi.aloisioUmberto.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Umberto Aloisio"
                ),
                description = "OpenApi documentation for Spring Security",
                title = "OpenAPi WManager",
                version = "1.0"
        ),
        servers = {
                @Server(
                        description = "local IT",
                        url = "http://192.168.15.34:8010"
                )

        }
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenApiConfig { // mette "in sicurezza" la documentazione di Swagger

}
