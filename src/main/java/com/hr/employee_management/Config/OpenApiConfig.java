package com.hr.employee_management.Config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    private static final String SECURITY_SCHEME_NAME = "BearerAuthentication";

    @Bean
    public OpenAPI customAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("/").description("Current Server (HTTPS / HTTP)")
                ))
                .info(new Info()
                        .title("HR & Payroll Management System REST API")
                        .version("1.0.0")
                        .description("Production-style HR and Payroll Management System REST API built with Spring Boot, " +
                                "PostgreSQL, Spring Security 6, and JWT. Features role-based access for ADMIN and HR, " +
                                "employee lifecycle management, attendance tracking, leave requests, and transactional payroll calculations.")
                        .contact(new Contact()
                                .name("Development Team")
                                .email("yimlemeng.ym@gmail.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Provide the JWT token obtained from /api/auth/login. Example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...'")));
    }
}
