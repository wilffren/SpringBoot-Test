package com.coopcredit.creditapplication.infrastructure.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "JWT Authorization header. First login at /api/auth/login to get token, then enter ONLY the token (without 'Bearer' prefix)"
)
public class OpenApiConfig {
    
    private static final String SECURITY_SCHEME_NAME = "bearerAuth";
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("CoopCredit API")
                        .version("1.0")
                        .description("""
                                ## Credit Application Service API
                                
                                ### Authentication Required
                                All endpoints except `/api/auth/**` require JWT authentication.
                                
                                ### How to authenticate:
                                1. Register: `POST /api/auth/register`
                                2. Login: `POST /api/auth/login` - Copy the token from response
                                3. Click the **Authorize** button (lock icon)
                                4. Paste ONLY the token (without 'Bearer' prefix)
                                5. Click **Authorize** - Lock icon will change to UNLOCKED
                                
                                ### Test Credentials
                                - Username: `admin`
                                - Password: `admin123`
                                """)
                        .contact(new Contact()
                                .name("CoopCredit Support")
                                .email("support@coopcredit.com")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new io.swagger.v3.oas.models.security.SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .in(io.swagger.v3.oas.models.security.SecurityScheme.In.HEADER)
                                        .description("Enter JWT token ONLY (without Bearer prefix)")));
    }
}