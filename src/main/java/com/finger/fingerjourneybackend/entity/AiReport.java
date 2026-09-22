// AiReport 엔티티
// ERD의 AI_REPORT 테이블과 매핑됨 (퀴즈+3행시 기반 AI 완주 리포트)
// keywords는 DB엔 콤마 구분 문자열로 저장하고, API 응답 시에만 배열로 변환함
// (서비스 레이어 책임 — ERD 컬럼 타입은 그대로 유지)

package com.finger.fingerjourneybackend.entity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AiReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    @Column(nullable = false, unique = true)
    private Long employeeId;

    // GENERATING / COMPLETED / FAILED
    @Column(nullable = false, length = 20)
    private String status;

    // @Lob만 쓰면 Postgres에서 text가 아니라 oid(대용량 객체)로 생성돼서 다루기 번거로움 → columnDefinition으로 명시
    @Column(columnDefinition = "text")
    private String reportContent;

    @Column(length = 500)
    private String keywords;

    // 리포트 결과 화면 상단 한 줄 타이틀 + 인용구 박스 (9/22 아티팩트 시안 복원하며 추가)
    @Column(length = 200)
    private String headline;

    @Column(length = 200)
    private String quote;

    @Column(length = 500)
    private String quoteDescription;

    private LocalDateTime generatedAt;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
