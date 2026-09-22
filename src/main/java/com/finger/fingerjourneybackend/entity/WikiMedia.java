// WikiMedia 엔티티
// ERD의 WIKI_MEDIA 테이블과 매핑됨 (위키 항목-미디어 다대다 연결, 최대 3장, 순서 포함)

package com.finger.fingerjourneybackend.entity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"wikiItemId", "mediaId"}))
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
public class WikiMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long wikiMediaId;

    @Column(nullable = false)
    private Long wikiItemId;

    @Column(nullable = false)
    private Long mediaId;

    @Column(nullable = false)
    private Integer displayOrder;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
