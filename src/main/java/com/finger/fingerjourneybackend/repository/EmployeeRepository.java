// EmployeeRepository
// 역할: Employee 테이블에 대한 DB 접근 담당 (Spring Data JPA)
// class가 아니라 interface로 바꾸고 JpaRepository<Employee, Long>을 상속받으면
// save/findAll/findById/deleteById 등 기본 CRUD 메서드는 자동 구현됨
//
// 예: public interface EmployeeRepository extends JpaRepository<Employee, Long> { }
//
// 담당 분배 (필요한 쿼리 메서드가 있으면 이 안에 선언):
// - 목록조회 → 재웅 (기본 findAll()로 충분할 수도 있음)
// - 상세조회/삭제 → 규원 (기본 findById/deleteById로 충분할 수도 있음)
// - 등록/수정 → 지연 (기본 save()로 충분할 수도 있음)

package com.finger.fingerjourneybackend.repository;

public class EmployeeRepository {
}
