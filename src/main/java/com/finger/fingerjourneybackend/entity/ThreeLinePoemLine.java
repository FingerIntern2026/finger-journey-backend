// ThreeLinePoemLine 엔티티
// ERD 원안엔 없던 신규 테이블. ThreeLinePoem의 line1~3 고정 컬럼을 대체하는
// 행 배열(1:N) 구조 — 이름 글자수(2~4자)만큼 가변으로 줄이 늘어나야 해서
// 한 사람의 3행시를 poemId 기준으로 여러 행(letter, text)에 나눠 저장함

package com.finger.fingerjourneybackend.entity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

// data.sql이 spring.sql.init.mode=always로 재시작마다 실행되는데,
// PK(lineId)가 매번 새로 채번돼서 ON CONFLICT DO NOTHING이 중복을 못 막던 버그가 있었음
// (poemId, lineOrder) 유니크 제약을 걸어 재시작해도 같은 줄이 안 쌓이게 함
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"poemId", "lineOrder"}))
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class ThreeLinePoemLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long lineId;

    @Column(nullable = false)
    private Long poemId;

    // 이 줄이 시작하는 이름 글자 (예: "김")
    @Column(nullable = false, length = 10)
    private String letter;

    @Column(nullable = false, length = 500)
    private String text;

    // 줄 순서 (0부터, 이름 글자 순서와 동일)
    @Column(nullable = false)
    private Integer lineOrder;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
