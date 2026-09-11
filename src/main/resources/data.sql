-- 테스트용 목(mock) 데이터
-- spring.sql.init.mode=always 라서 앱 실행할 때마다 실행됨
-- ON CONFLICT DO NOTHING으로 재실행해도 중복 에러 안 나게 처리

INSERT INTO organization (organization_id, organization_name, organization_type, parent_id) VALUES
    (1, '개발본부', 'DIVISION', NULL),
    (2, '개발1팀', 'TEAM', 1)
ON CONFLICT (organization_id) DO NOTHING;

INSERT INTO position (position_id, position_name, position_order) VALUES
    (1, '사원', 1),
    (2, '대리', 2)
ON CONFLICT (position_id) DO NOTHING;

INSERT INTO employee (employee_id, employee_no, name, organization_id, position_id, hire_date, current_phase) VALUES
    (1, '12609001', '김신입', 2, 1, '2026-08-01', 'ONBOARDING'),
    (2, '12609002', '이신입', 2, 1, '2026-08-05', 'PREBOARDING'),
    (3, '12500001', '박기존', 1, 2, '2020-03-01', 'COMPLETED')
ON CONFLICT (employee_id) DO NOTHING;


-- mock 데이터를 employee_id를 직접 지정해서 넣었기 때문에,
-- 시퀀스(자동 증가 번호표 발급기)가 이 사실을 몰라 다음 등록 시 번호 충돌이 남
-- 그래서 mock 데이터 삽입 후 시퀀스를 현재 최대값 기준으로 재설정해줌
SELECT setval('employee_employee_id_seq', (SELECT MAX(employee_id) FROM employee));
