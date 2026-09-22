// ThreeLinePoem 엔티티
// ERD의 THREE_LINE_POEM 테이블과 매핑되나, 원래 ERD의 line1~3 고정 3컬럼 대신
// 이름 글자수(2~4자)만큼 가변으로 늘어나는 행 배열 구조로 변경함 (9/22 팀 설계 반영)
// 실제 줄 내용은 ThreeLinePoemLine에 1:N으로 저장됨

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
public class ThreeLinePoem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long poemId;

    @Column(nullable = false, unique = true)
    private Long employeeId;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
