package com.flowday.flowday.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Autowired
    private AppProperties appProperties;

    @Bean
    public OpenAPI flowdayOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title(appProperties.getBrand().getName() + " API")
                        .description("API REST v1 — preparada para frontend React")
                        .version("1.0.0"));
    }
}
