// EmployeeService
// 역할: 실제 비즈니스 로직 담당 (Controller와 Repository 사이 계층)
// EmployeeRepository를 주입받아서 사용, 예외 처리/데이터 가공 등은 여기서 함
//
// 담당 분배:
// - 목록조회 로직 → 재웅
// - 상세조회/삭제 로직 → 규원 (상세조회 시 대상 없으면 COMMON_404 처리 필요)
// - 등록/수정 로직 → 지연 (등록 시 employeeNo 중복이면 ADM_003 에러 처리,
//                        생성 시 currentPhase는 "PREBOARDING"으로 고정)

package com.finger.fingerjourneybackend.service;


import com.finger.fingerjourneybackend.entity.Employee;
import com.finger.fingerjourneybackend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor

public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public List<Employee> getEmployeeList() {
        return employeeRepository.findAll();
    }
}
