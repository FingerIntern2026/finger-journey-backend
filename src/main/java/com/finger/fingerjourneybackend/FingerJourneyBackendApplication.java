package com.finger.fingerjourneybackend;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@SpringBootApplication
@EnableJpaAuditing
public class FingerJourneyBackendApplication {

    public static void main(String[] args) {
        loadDotenv();
        SpringApplication.run(FingerJourneyBackendApplication.class, args);
    }

    // 프로젝트 루트의 .env 파일을 읽어서 JVM 시스템 프로퍼티로 등록한다.
    // application.properties의 ${DB_URL} 같은 플레이스홀더가 이 값을 참조한다.
    private static void loadDotenv() {
        Path envFile = Path.of(".env");
        if (!Files.exists(envFile)) {
            return;
        }
        try {
            List<String> lines = Files.readAllLines(envFile);
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int separatorIndex = trimmed.indexOf('=');
                if (separatorIndex == -1) {
                    continue;
                }
                String key = trimmed.substring(0, separatorIndex).trim();
                String value = trimmed.substring(separatorIndex + 1).trim();
                System.setProperty(key, value);
            }
        } catch (IOException e) {
            throw new RuntimeException(".env 파일을 읽는 중 오류가 발생했습니다.", e);
        }
    }

}
