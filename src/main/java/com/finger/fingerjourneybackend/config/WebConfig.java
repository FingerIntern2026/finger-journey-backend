// WebConfig
// 역할
// 1. 프론트(Vite 개발서버, localhost:5173)에서 백엔드(localhost:8080) API를
//    호출할 수 있도록 CORS(다른 출처 간 요청)를 허용
// 2. 관리자 API(/admin/**)에 권한 검사(AdminAuthInterceptor)를 적용
// 3. 관리자가 올린 이미지(/media/**)를 업로드 폴더에서 내려줌
//
// 프론트/백엔드가 서로 다른 포트(=다른 출처)라서, CORS 설정 없이는 브라우저가
// 보안상 요청 자체를 막아버림 (콘솔에 CORS 에러가 뜸)

package com.finger.fingerjourneybackend.config;

import com.finger.fingerjourneybackend.service.MediaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AdminAuthInterceptor adminAuthInterceptor;
    private final boolean adminAuthEnabled;
    private final Path mediaUploadDir;

    // 프론트가 토큰을 보내기 전까지는 false로 두고, 로그인 연동 후 true로 바꾼다.
    public WebConfig(
            AdminAuthInterceptor adminAuthInterceptor,
            @Value("${app.admin-auth.enabled:false}") boolean adminAuthEnabled,
            @Value("${app.media.upload-dir:./uploads}") String mediaUploadDir) {
        this.adminAuthInterceptor = adminAuthInterceptor;
        this.adminAuthEnabled = adminAuthEnabled;
        this.mediaUploadDir = Path.of(mediaUploadDir).toAbsolutePath().normalize();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (!adminAuthEnabled) {
            return;
        }

        // 화면정보 목록은 로그인 전 앱 시작 때 호출하므로 인증 없이 연다. (API-ADM-018)
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns("/admin/**")
                .excludePathPatterns("/admin/screen/list");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 폴더 위치는 끝에 /가 있어야 그 안의 파일로 인식된다.
        String location = mediaUploadDir.toUri().toString();
        if (!location.endsWith("/")) {
            location += "/";
        }

        registry.addResourceHandler(MediaService.URL_PREFIX + "**")
                .addResourceLocations(location);
    }
}
