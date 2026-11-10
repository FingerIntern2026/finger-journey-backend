package com.finger.fingerjourneybackend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI fingerJourneyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Finger Journey API")
                        .description("핑거저니 온보딩 서비스 API")
                        .version("v1"));
    }
}
