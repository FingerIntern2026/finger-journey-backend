package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.config.JwtTokenProvider;
import com.finger.fingerjourneybackend.dto.auth.request.LoginRequestDto;
import com.finger.fingerjourneybackend.dto.auth.request.TokenRefreshRequestDto;
import com.finger.fingerjourneybackend.dto.auth.response.TokenRefreshResponseDto;
import com.finger.fingerjourneybackend.dto.auth.response.LoginResponseDto;
import com.finger.fingerjourneybackend.dto.auth.response.SessionResponseDto;
import com.finger.fingerjourneybackend.entity.Employee;
import com.finger.fingerjourneybackend.entity.Admin;
import com.finger.fingerjourneybackend.entity.Organization;
import com.finger.fingerjourneybackend.entity.Position;
import com.finger.fingerjourneybackend.entity.RefreshToken;
import com.finger.fingerjourneybackend.exception.CustomException;
import com.finger.fingerjourneybackend.exception.ErrorCode;
import com.finger.fingerjourneybackend.repository.EmployeeRepository;
import com.finger.fingerjourneybackend.repository.AdminRepository;
import com.finger.fingerjourneybackend.repository.OrganizationRepository;
import com.finger.fingerjourneybackend.repository.PositionRepository;
import com.finger.fingerjourneybackend.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private static final DateTimeFormatter BIRTH_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("uuuuMMdd").withResolverStyle(ResolverStyle.STRICT);

    private final EmployeeRepository employeeRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final OrganizationRepository organizationRepository;
    private final PositionRepository positionRepository;
    private final AdminRepository adminRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    // 사번과 생년월일을 검증하고, 직원 정보와 발급한 토큰을 반환한다.
    @Transactional
    public LoginResponseDto authenticate(LoginRequestDto request) {
        Employee employee = employeeRepository.findByEmployeeNo(request.getEmpNo())
                .orElseThrow(() -> new CustomException(ErrorCode.LOGIN_FAILED));

        String birthDateText = request.getBirthDate();
        if (birthDateText == null || !birthDateText.matches("[0-9]{8}")) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }

        LocalDate birthDate;
        try {
            // STRICT: 2월 31일처럼 존재하지 않는 날짜를 자동 보정하지 않고 거절한다.
            birthDate = LocalDate.parse(birthDateText, BIRTH_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }

        if (!birthDate.equals(employee.getBirthDate())) {
            throw new CustomException(ErrorCode.LOGIN_FAILED);
        }

        String organizationName = organizationRepository.findById(employee.getOrganizationId())
                .map(Organization::getOrganizationName)
                .orElse(null);
        String positionName = positionRepository.findById(employee.getPositionId())
                .map(Position::getPositionName)
                .orElse(null);

        String adminRole = adminRepository.findByEmployeeId(employee.getEmployeeId())
                .map(Admin::getRole)
                .orElse(null);

        String redirectScreen = resolveRedirectScreen(employee.getCurrentPhase());

        String accessToken = jwtTokenProvider.createAccessToken(employee.getEmployeeId());
        String refreshToken = jwtTokenProvider.createRefreshToken(employee.getEmployeeId());

        RefreshToken tokenRecord = new RefreshToken();
        tokenRecord.setEmployeeId(employee.getEmployeeId());
        tokenRecord.setTokenHash(hashToken(refreshToken));
        tokenRecord.setExpiresAt(jwtTokenProvider.getRefreshTokenExpiration(refreshToken));
        refreshTokenRepository.save(tokenRecord);

        return LoginResponseDto.builder()
                .employeeId(employee.getEmployeeId())
                .name(employee.getName())
                .organizationName(organizationName)
                .positionName(positionName)
                .currentPhase(employee.getCurrentPhase())
                .adminRole(adminRole)
                .redirectScreen(redirectScreen)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Transactional
    public TokenRefreshResponseDto refresh(TokenRefreshRequestDto request) {
        String oldToken = request.getRefreshToken();
        Long employeeId = jwtTokenProvider.validateRefreshToken(oldToken);

        RefreshToken tokenRecord = refreshTokenRepository.findByTokenHash(hashToken(oldToken))
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_SESSION));

        if (!employeeId.equals(tokenRecord.getEmployeeId())) {
            throw new CustomException(ErrorCode.INVALID_SESSION);
        }
        if (!tokenRecord.getExpiresAt().isAfter(Instant.now())) {
            throw new CustomException(ErrorCode.REFRESH_TOKEN_EXPIRED);
        }
        if (!employeeRepository.existsById(employeeId)) {
            throw new CustomException(ErrorCode.INVALID_SESSION);
        }

        String accessToken = jwtTokenProvider.createAccessToken(employeeId);
        String refreshToken = jwtTokenProvider.createRefreshToken(employeeId);

        // 기존 해시를 교체하므로 이전 토큰으로는 더 이상 기록을 찾을 수 없다.
        tokenRecord.setTokenHash(hashToken(refreshToken));
        tokenRecord.setExpiresAt(jwtTokenProvider.getRefreshTokenExpiration(refreshToken));
        refreshTokenRepository.save(tokenRecord);

        return TokenRefreshResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    public Long getEmployeeId(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.INVALID_SESSION);
        }

        String accessToken = authorization.substring(7);

        return jwtTokenProvider.validateAccessToken(accessToken);
    }

    public SessionResponseDto getSession(String authorization) {
        Long employeeId = getEmployeeId(authorization);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_SESSION));

        return SessionResponseDto.builder()
                .valid(true)
                .employeeId(employee.getEmployeeId())
                .currentPhase(employee.getCurrentPhase())
                .build();
    }

    // 토큰 원문 대신 DB에 저장하고 비교할 SHA-256 해시값을 만든다.
    private String hashToken(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 해시 기능을 사용할 수 없습니다.", e);
        }
    }

    // 현재 진행 단계에 맞는 로그인 후 진입 화면을 반환한다.
    private String resolveRedirectScreen(String currentPhase) {
        if (currentPhase == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        return switch (currentPhase) {
            case "PREBOARDING" -> "PB_LGN_P02";
            case "BAG" -> "CHK_INT_P01";
            case "ONBOARDING" -> "PTH_MAP_P01";
            case "COMPLETED" -> "PTH_CPL_P03";
            default -> throw new CustomException(ErrorCode.INVALID_REQUEST);
        };
    }
}
