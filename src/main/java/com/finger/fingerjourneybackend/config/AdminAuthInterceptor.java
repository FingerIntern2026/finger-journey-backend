// AdminAuthInterceptor
// 역할: 관리자 API(/admin/**) 요청이 컨트롤러에 도착하기 전에 권한을 검사한다.
//
// 검사 순서
// 1. Authorization: Bearer 토큰 확인 (만료 → AUTH_401, 위조·누락 → AUTH_002)
// 2. 토큰의 사원 ID로 admin 테이블 조회
// 3. role이 HR 또는 ADMIN이 아니면 ADMIN_ACCESS_DENIED (ADM_001, 403)
//
// 적용 범위와 켜고 끄는 설정은 WebConfig에서 관리한다.

package com.finger.fingerjourneybackend.config;

import com.finger.fingerjourneybackend.entity.Admin;
import com.finger.fingerjourneybackend.exception.CustomException;
import com.finger.fingerjourneybackend.exception.ErrorCode;
import com.finger.fingerjourneybackend.repository.AdminRepository;
import com.finger.fingerjourneybackend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {

    // 통과한 요청에 관리자의 사원 ID를 담아 둔다. (확인자 기록 등에 컨트롤러가 꺼내 쓴다)
    public static final String ADMIN_EMPLOYEE_ID_ATTRIBUTE = "adminEmployeeId";

    private static final Set<String> ADMIN_ROLES = Set.of("HR", "ADMIN");

    private final AuthService authService;
    private final AdminRepository adminRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // CORS 사전 요청(OPTIONS)에는 토큰이 없으므로 검사하지 않는다.
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        Long employeeId = authService.getEmployeeId(request.getHeader(HttpHeaders.AUTHORIZATION));

        boolean isAdmin = adminRepository.findByEmployeeId(employeeId)
                .map(Admin::getRole)
                .filter(ADMIN_ROLES::contains)
                .isPresent();

        if (!isAdmin) {
            throw new CustomException(ErrorCode.ADMIN_ACCESS_DENIED);
        }

        request.setAttribute(ADMIN_EMPLOYEE_ID_ATTRIBUTE, employeeId);
        return true;
    }
}
