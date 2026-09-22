// AiServerProperties
// finger-journey-ai(FastAPI) 서버 접속 정보를 application.properties에서 읽어옴
// 예: ai.server.base-url=http://localhost:8000

package com.finger.fingerjourneybackend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "ai.server")
@Getter
@Setter
public class AiServerProperties {
    private String baseUrl = "http://localhost:8000";
}
