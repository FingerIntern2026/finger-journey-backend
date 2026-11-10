package com.finger.fingerjourneybackend.service;

import com.finger.fingerjourneybackend.config.JwtTokenProvider;
import com.finger.fingerjourneybackend.dto.auth.request.LoginRequestDto;
import com.finger.fingerjourneybackend.dto.auth.response.LoginResponseDto;
import com.finger.fingerjourneybackend.dto.auth.response.SessionResponseDto;
import com.finger.fingerjourneybackend.entity.Employee;
import com.finger.fingerjourneybackend.entity.Admin;
import com.finger.fingerjourneybackend.entity.Organization;
import com.finger.fingerjourneybackend.entity.Position;
import com.finger.fingerjourneybackend.exception.CustomException;
import com.finger.fingerjourneybackend.exception.ErrorCode;
import com.finger.fingerjourneybackend.repository.EmployeeRepository;
import com.finger.fingerjourneybackend.repository.AdminRepository;
import com.finger.fingerjourneybackend.repository.OrganizationRepository;
import com.finger.fingerjourneybackend.repository.PositionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

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

    // 사번과 생년월일을 검증하고, 직원 정보와 발급한 토큰을 반환한다.
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

    public SessionResponseDto getSession(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.INVALID_SESSION);
        }

        Long employeeId = jwtTokenProvider.validateAccessToken(authorization.substring(7));
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_SESSION));

        return SessionResponseDto.builder()
                .valid(true)
                .employeeId(employee.getEmployeeId())
                .currentPhase(employee.getCurrentPhase())
                .build();
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
