// WebConfig
// 역할: 프론트(Vite 개발서버, localhost:5173)에서 백엔드(localhost:8080) API를
// 호출할 수 있도록 CORS(다른 출처 간 요청)를 허용하는 설정
//
// 프론트/백엔드가 서로 다른 포트(=다른 출처)라서, 이 설정 없이는 브라우저가
// 보안상 요청 자체를 막아버림 (콘솔에 CORS 에러가 뜸)

package com.finger.fingerjourneybackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
