// AppSetting 엔티티
// 역할: 서비스 전체에서 값 하나로 관리하는 설정을 저장한다. (key-value)
// 지금은 HR이 정하는 체크인 비밀번호(CHECKIN_PASSWORD) 하나만 사용한다.

package com.finger.fingerjourneybackend.entity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AppSetting {

    @Id
    @Column(length = 50)
    private String settingKey;

    @Column(nullable = false, length = 255)
    private String settingValue;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
