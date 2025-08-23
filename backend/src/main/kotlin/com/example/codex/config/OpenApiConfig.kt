package com.example.codex.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    @Bean
    fun api(): OpenAPI =
        OpenAPI()
            .info(
                Info()
                    .title("Codex API")
                    .version("v1"),
            )
}
