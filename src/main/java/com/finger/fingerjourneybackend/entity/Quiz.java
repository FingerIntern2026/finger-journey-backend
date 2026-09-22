// Quiz 엔티티
// ERD의 QUIZ 테이블과 매핑됨 (징검다리 퀴즈 문항, 4지선다)

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
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long quizId;

    @Column(nullable = false, length = 500)
    private String question;

    @Column(nullable = false, length = 255)
    private String option1;

    @Column(nullable = false, length = 255)
    private String option2;

    @Column(nullable = false, length = 255)
    private String option3;

    @Column(nullable = false, length = 255)
    private String option4;

    @Column(nullable = false)
    private Integer displayOrder;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
