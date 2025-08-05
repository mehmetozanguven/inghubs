package com.mehmetozanguven.inghubs_digital_wallet.core.configuration;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AppSwaggerConfiguration {

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP)
                .bearerFormat("JWT")
                .scheme("bearer");
    }

    @Bean
    public OpenAPI customOpenAPI() {
        SecurityRequirement bearerReq = new SecurityRequirement();
        bearerReq.addList("Bearer Authentication");
        Components components = new Components();
        components.addSecuritySchemes("Bearer Authentication", createAPIKeyScheme());

        return new OpenAPI()
                .addSecurityItem(bearerReq)
                .components(components)
                .info(new Info()
                        .title("INGHubs Rest API")
                        .description(" INGHubs - Software Engineer Assignment")
                        .version("1")
                        .license(new License().name("Apache 2.0")));
    }
}
