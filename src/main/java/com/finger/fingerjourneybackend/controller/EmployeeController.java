// EmployeeController
// 역할: HTTP 요청을 받아서 EmployeeService를 호출하고, 응답 형식({success, data} / {success, errorCode, message})으로 감싸서 리턴하는 계층
// 실제 비즈니스 로직(DB 조회/저장)은 여기 두지 않고 Service에 위임할 것
//
// API 명세서 기준 담당 분배:
// - POST /admin/employee/list   (목록조회, ADM-001) → 재웅
// - POST /admin/employee/detail (상세조회, ADM-002) → 규원
// - POST /admin/employee/create (등록,   ADM-003) → 지연
// - POST /admin/employee/update (수정,   ADM-004) → 지연
// - POST /admin/employee/delete (삭제,   ADM-005) → 규원

package com.finger.fingerjourneybackend.controller;

public class EmployeeController {
}
