// RestClientConfig
// finger-journey-ai(FastAPI) 서버를 호출할 때 쓰는 RestClient 빈 등록
// WebClient(webflux 의존성 필요)까지는 필요 없어서, spring-web에 기본 포함된
// RestClient(Spring 6.1+)로 감
//
// 주의 : RestClient의 기본 요청 팩토리(JDK HttpClient)가 HTTP/2를 시도하는데,
// uvicorn(h11)이 이걸 이해하지 못해 요청 바디가 통째로 깨지는 문제가 있었음
// (uvicorn 로그에 "Unsupported upgrade request" 경고) → HTTP/1.1로 고정해서 해결

package com.finger.fingerjourneybackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import lombok.RequiredArgsConstructor;

import java.net.http.HttpClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final AiServerProperties aiServerProperties;

    @Bean
    public RestClient aiRestClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        return RestClient.builder()
                .baseUrl(aiServerProperties.getBaseUrl())
                .requestFactory(new JdkClientHttpRequestFactory(httpClient))
                .build();
    }
}
