package com.finger.fingerjourneybackend.repository;

import com.finger.fingerjourneybackend.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // 같은 토큰의 동시 재발급을 막기 위해 조회한 기록을 잠근다.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    void deleteByEmployeeId(Long employeeId);
}
