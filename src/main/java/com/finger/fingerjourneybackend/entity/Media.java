// Media 엔티티
// ERD의 MEDIA 테이블과 매핑됨 (이미지/아이콘/배경 등 파일 메타데이터)

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
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mediaId;

    @Column(nullable = false, length = 500)
    private String fileUrl;

    // IMAGE / ICON / BACKGROUND
    @Column(nullable = false, length = 30)
    private String mediaType;

    @Column(length = 255)
    private String altText;

    @Column(length = 255)
    private String originalFileName;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
