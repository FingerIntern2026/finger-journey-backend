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
    (3, '12500001', '박기존', 1, 2, '2020-03-01', 'COMPLETED'),
    (4, '12609004', '최준비', 2, 1, '2026-09-01', 'ONBOARDING'),
    (5, '12609005', '정발표', 2, 1, '2026-09-10', 'ONBOARDING'),
    (6, '12609006', '한마무리', 2, 1, '2026-09-15', 'ONBOARDING'),
    (7, '12609007', '오검증', 2, 1, '2026-09-18', 'ONBOARDING')
ON CONFLICT (employee_id) DO NOTHING;


-- mock 데이터를 id를 직접 지정해서 넣었기 때문에,
-- 시퀀스(자동 증가 번호표 발급기)가 이 사실을 몰라 다음 등록 시 번호 충돌이 남
-- 그래서 mock 데이터 삽입 후 시퀀스를 현재 최대값 기준으로 재설정해줌
-- (organization/position은 지금 등록 API가 없어서 당장 안 터지지만, 나중에 생기면 employee와 똑같이 충돌하므로 미리 맞춰둠)
SELECT setval('organization_organization_id_seq', (SELECT MAX(organization_id) FROM organization));
SELECT setval('position_position_id_seq', (SELECT MAX(position_id) FROM position));
SELECT setval('employee_employee_id_seq', (SELECT MAX(employee_id) FROM employee));


-- 기존 데모 화면 시드: 아래의 실제 서비스 화면정보로 전환하기 전에 정리 대상만 식별한다.
/*
INSERT INTO task (task_code, task_name, entry_screen_code, exit_screen_code) VALUES
    ('DEMO', '프론트 기능 데모', 'DEMO_HOM_P01', 'DEMO_HOM_P01')
ON CONFLICT (task_code) DO UPDATE SET
    task_name = EXCLUDED.task_name,
    entry_screen_code = EXCLUDED.entry_screen_code,
    exit_screen_code = EXCLUDED.exit_screen_code;
*/

-- 기존 DEMO-001 형식의 개발용 화면 시드만 한 번 정리한다.
-- 새 화면 코드는 삭제하지 않으므로 서버를 재실행해도 screen_id가 유지된다.
DELETE FROM screen_info
WHERE screen_code IN (
    'DEMO-001',
    'DEMO-002',
    'DEMO-003',
    'DEMO-004',
    'DEMO-005',
    'DEMO-006',
    'DEMO-007',
    'DEMO-008',
    'DEMO-009',
    'DEMO-010',
    'DEMO-011'
);

/*
INSERT INTO screen_info (
    task_id,
    screen_code,
    screen_name,
    route_path,
    file_path,
    login_required,
    back_action,
    back_screen_code,
    display_order
) VALUES
    (
        (SELECT task_id FROM task WHERE task_code = 'DEMO'),
        'DEMO_HOM_P01', '데모 홈', '/demo', 'demo/DEMO_HOM_P01_DemoIndexPage.jsx', FALSE,
        'BLOCK', NULL, 1
    ),
    (
        (SELECT task_id FROM task WHERE task_code = 'DEMO'),
        'DEMO_MOV_P01', '화면 이동 가이드', '/demo/move', 'demo/DEMO_MOV_P01_MoveGuidePage.jsx', FALSE,
        'TARGET', 'DEMO_HOM_P01', 2
    ),
    (
        (SELECT task_id FROM task WHERE task_code = 'DEMO'),
        'DEMO_AUT_P01', '권한 검사', '/demo/move/auth-check', 'demo/DEMO_AUT_P01_AuthCheckPage.jsx', TRUE,
        'TARGET', 'DEMO_MOV_P01', 3
    ),
    (
        (SELECT task_id FROM task WHERE task_code = 'DEMO'),
        'DEMO_PRM_P01', '파라미터 전달', '/demo/move/param', 'demo/DEMO_PRM_P01_ParamPassPage.jsx', FALSE,
        'TARGET', 'DEMO_MOV_P01', 4
    ),
    (
        (SELECT task_id FROM task WHERE task_code = 'DEMO'),
        'DEMO_PRM_P02', '파라미터 상세', '/demo/param-detail', 'demo/DEMO_PRM_P02_ParamDetailPage.jsx', FALSE,
        'TARGET', 'DEMO_PRM_P01', 5
    ),
    (
        (SELECT task_id FROM task WHERE task_code = 'DEMO'),
        'DEMO_MOV_P02', '뒤로가기 예제', '/demo/move/go-back', 'demo/DEMO_MOV_P02_GoBackExamplePage.jsx', FALSE,
        'TARGET', 'DEMO_MOV_P01', 6
    ),
    (
        (SELECT task_id FROM task WHERE task_code = 'DEMO'),
        'DEMO_CMP_P01', '컴포넌트 목록', '/demo/components', 'demo/DEMO_CMP_P01_ComponentListPage.jsx', FALSE,
        'TARGET', 'DEMO_HOM_P01', 7
    ),
    (
        (SELECT task_id FROM task WHERE task_code = 'DEMO'),
        'DEMO_DLG_P01', '다이얼로그 예제', '/demo/dialog', 'demo/DEMO_DLG_P01_DialogExamplePage.jsx', FALSE,
        'TARGET', 'DEMO_HOM_P01', 8
    ),
    (
        (SELECT task_id FROM task WHERE task_code = 'DEMO'),
        'DEMO_API_P01', 'API 통신 예제', '/demo/api', 'demo/DEMO_API_P01_ApiExamplePage.jsx', FALSE,
        'TARGET', 'DEMO_HOM_P01', 9
    ),
    (
        (SELECT task_id FROM task WHERE task_code = 'DEMO'),
        'DEMO_RPT_P01', 'AI 완주 여정', '/demo/report', 'demo/DEMO_RPT_P01_ReportExamplePage.jsx', FALSE,
        'TARGET', 'DEMO_HOM_P01', 10
    ),
    (
        (SELECT task_id FROM task WHERE task_code = 'DEMO'),
        'DEMO_RPT_P02', 'AI 완주 리포트 결과', '/demo/report/result', 'demo/DEMO_RPT_P02_ReportResultPage.jsx', FALSE,
        'TARGET', 'DEMO_RPT_P01', 11
    )
ON CONFLICT (screen_code) DO UPDATE SET
    task_id = EXCLUDED.task_id,
    screen_name = EXCLUDED.screen_name,
    route_path = EXCLUDED.route_path,
    file_path = EXCLUDED.file_path,
    login_required = EXCLUDED.login_required,
    back_action = EXCLUDED.back_action,
    back_screen_code = EXCLUDED.back_screen_code,
    display_order = EXCLUDED.display_order;
*/


-- 실제 서비스 화면정보
-- 화면은 screenCode → routePath → filePath 순으로 프론트의 동적 로더가 연결한다.
-- 발표 환경에서는 로그인 여부와 관계없이 모든 화면을 열 수 있도록 login_required를 FALSE로 둔다.
DELETE FROM screen_info WHERE screen_code LIKE 'DEMO_%';
DELETE FROM task WHERE task_code = 'DEMO';

INSERT INTO task (task_code, task_name, entry_screen_code, exit_screen_code) VALUES
    ('PB', '프리보딩', 'PB_LGN_P01', 'PB_LGN_P02'),
    ('CHK', '체크인', 'CHK_INT_P01', 'CHK_FIN_P01'),
    ('PTH', '오솔길', 'PTH_MAP_P01', 'PTH_CPL_P03'),
    ('WIK', '핑거 위키', 'WIK_INT_P01', 'WIK_LIS_P01'),
    ('ADM', '관리자', 'ADM_EMP_P01', 'ADM_HRC_P01'),
    ('CHT', 'AI 챗봇', 'CHT_MAN_F01', 'CHT_MAN_F01')
ON CONFLICT (task_code) DO UPDATE SET
    task_name = EXCLUDED.task_name,
    entry_screen_code = EXCLUDED.entry_screen_code,
    exit_screen_code = EXCLUDED.exit_screen_code;
INSERT INTO screen_info (
    task_id, screen_code, screen_name, route_path, file_path,
    login_required, back_action, back_screen_code, display_order
) VALUES
    ((SELECT task_id FROM task WHERE task_code = 'PB'), 'PB_LGN_P01', '프리보딩 인증', '/preboarding/login', 'pb/PB_LGN_P01.jsx', FALSE, 'BLOCK', NULL, 1),
    ((SELECT task_id FROM task WHERE task_code = 'PB'), 'PB_LGN_P02', '프리보딩 완료', '/preboarding/welcome', 'pb/PB_LGN_P02.jsx', FALSE, 'BLOCK', NULL, 2),

    ((SELECT task_id FROM task WHERE task_code = 'CHK'), 'CHK_INT_P01', '체크인 비밀번호', '/checkin/password', 'chk/CHK_INT_P01.jsx', FALSE, 'BLOCK', NULL, 1),
    ((SELECT task_id FROM task WHERE task_code = 'CHK'), 'CHK_EXP_P01', '출근 준비물', '/checkin/packing', 'chk/CHK_EXP_P01.jsx', FALSE, 'BLOCK', NULL, 2),
    ((SELECT task_id FROM task WHERE task_code = 'CHK'), 'CHK_DOC_P01', '서류 확인', '/checkin/documents', 'chk/CHK_DOC_P01.jsx', FALSE, 'BLOCK', NULL, 3),
    ((SELECT task_id FROM task WHERE task_code = 'CHK'), 'CHK_EQU_P01', '장비 확인', '/checkin/equipment', 'chk/CHK_EQU_P01.jsx', FALSE, 'BLOCK', NULL, 4),
    ((SELECT task_id FROM task WHERE task_code = 'CHK'), 'CHK_ACC_P01', 'ERP 계정', '/checkin/erp-account', 'chk/CHK_ACC_P01.jsx', FALSE, 'BLOCK', NULL, 5),
    ((SELECT task_id FROM task WHERE task_code = 'CHK'), 'CHK_ACC_P02', '포레스트 로그인', '/checkin/forest-login', 'chk/CHK_ACC_P02.jsx', FALSE, 'BLOCK', NULL, 6),
    ((SELECT task_id FROM task WHERE task_code = 'CHK'), 'CHK_APR_P01', '결재 시작', '/checkin/approval', 'chk/CHK_APR_P01.jsx', FALSE, 'BLOCK', NULL, 7),
    ((SELECT task_id FROM task WHERE task_code = 'CHK'), 'CHK_APR_P02', '결재 작성', '/checkin/approval/guide', 'chk/CHK_APR_P02.jsx', FALSE, 'BLOCK', NULL, 8),
    ((SELECT task_id FROM task WHERE task_code = 'CHK'), 'CHK_APR_P03', '결재 완료', '/checkin/approval/complete', 'chk/CHK_APR_P03.jsx', FALSE, 'BLOCK', NULL, 9),
    ((SELECT task_id FROM task WHERE task_code = 'CHK'), 'CHK_SEA_P01', '좌석 선택', '/checkin/seat', 'chk/CHK_SEA_P01.jsx', FALSE, 'BLOCK', NULL, 10),
    ((SELECT task_id FROM task WHERE task_code = 'CHK'), 'CHK_SEA_P02', '종로 포레스트', '/checkin/seat/jongno', 'chk/CHK_SEA_P02.jsx', FALSE, 'BLOCK', NULL, 11),
    ((SELECT task_id FROM task WHERE task_code = 'CHK'), 'CHK_SEA_P03', '여의도 포레스트', '/checkin/seat/yeouido', 'chk/CHK_SEA_P03.jsx', FALSE, 'BLOCK', NULL, 12),
    ((SELECT task_id FROM task WHERE task_code = 'CHK'), 'CHK_FIN_P01', '체크인 완료', '/checkin/complete', 'chk/CHK_FIN_P01.jsx', FALSE, 'BLOCK', NULL, 13),

    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_MAP_P01', '오솔길 지도', '/path/map', 'pth/PTH_MAP_P01.jsx', FALSE, 'BLOCK', NULL, 1),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_STR_P01', '핑거 이야기 1', '/path/story/1', 'pth/str/PTH_STR_P01.jsx', FALSE, 'BLOCK', NULL, 2),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_STR_P02', '핑거 이야기 2', '/path/story/2', 'pth/str/PTH_STR_P02.jsx', FALSE, 'BLOCK', NULL, 3),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_STR_P03', '핑거 이야기 3', '/path/story/3', 'pth/str/PTH_STR_P03.jsx', FALSE, 'BLOCK', NULL, 4),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_STR_P04', '핑거 이야기 4', '/path/story/4', 'pth/str/PTH_STR_P04.jsx', FALSE, 'BLOCK', NULL, 5),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_STR_P05', '핑거 이야기 5', '/path/story/5', 'pth/str/PTH_STR_P05.jsx', FALSE, 'BLOCK', NULL, 6),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_STR_P06', '핑거 이야기 6', '/path/story/6', 'pth/str/PTH_STR_P06.jsx', FALSE, 'BLOCK', NULL, 7),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_STR_P07', '핑거 이야기 완료', '/path/story/complete', 'pth/str/PTH_STR_P07.jsx', FALSE, 'BLOCK', NULL, 8),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_RUL_P01', '취업규칙 1', '/path/rule/1', 'pth/rul/PTH_RUL_P01.jsx', FALSE, 'BLOCK', NULL, 9),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_RUL_P02', '취업규칙 2', '/path/rule/2', 'pth/rul/PTH_RUL_P02.jsx', FALSE, 'BLOCK', NULL, 10),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_RUL_P03', '취업규칙 3', '/path/rule/3', 'pth/rul/PTH_RUL_P03.jsx', FALSE, 'BLOCK', NULL, 11),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_RUL_P04', '취업규칙 4', '/path/rule/4', 'pth/rul/PTH_RUL_P04.jsx', FALSE, 'BLOCK', NULL, 12),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_RUL_P05', '취업규칙 5', '/path/rule/5', 'pth/rul/PTH_RUL_P05.jsx', FALSE, 'BLOCK', NULL, 13),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_RUL_P06', '취업규칙 6', '/path/rule/6', 'pth/rul/PTH_RUL_P06.jsx', FALSE, 'BLOCK', NULL, 14),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_RUL_P07', '취업규칙 완료', '/path/rule/complete', 'pth/rul/PTH_RUL_P07.jsx', FALSE, 'BLOCK', NULL, 15),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_HRP_P01', '인사규정 1', '/path/hr-policy/1', 'pth/hrp/PTH_HRP_P01.jsx', FALSE, 'BLOCK', NULL, 16),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_HRP_P02', '인사규정 2', '/path/hr-policy/2', 'pth/hrp/PTH_HRP_P02.jsx', FALSE, 'BLOCK', NULL, 17),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_HRP_P03', '인사규정 3', '/path/hr-policy/3', 'pth/hrp/PTH_HRP_P03.jsx', FALSE, 'BLOCK', NULL, 18),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_HRP_P04', '인사규정 완료', '/path/hr-policy/complete', 'pth/hrp/PTH_HRP_P04.jsx', FALSE, 'BLOCK', NULL, 19),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_ETH_P01', '윤리규정 1', '/path/ethics/1', 'pth/rth/PTH_ETH_P01.jsx', FALSE, 'BLOCK', NULL, 20),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_ETH_P02', '윤리규정 2', '/path/ethics/2', 'pth/rth/PTH_ETH_P02.jsx', FALSE, 'BLOCK', NULL, 21),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_ETH_P03', '윤리규정 3', '/path/ethics/3', 'pth/rth/PTH_ETH_P03.jsx', FALSE, 'BLOCK', NULL, 22),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_ETH_P04', '윤리규정 4', '/path/ethics/4', 'pth/rth/PTH_ETH_P04.jsx', FALSE, 'BLOCK', NULL, 23),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_ETH_P05', '윤리규정 완료', '/path/ethics/complete', 'pth/rth/PTH_ETH_P05.jsx', FALSE, 'BLOCK', NULL, 24),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_EML_P01', '이메일 작성', '/path/email/1', 'pth/eml/PTH_EML_P01.jsx', FALSE, 'BLOCK', NULL, 25),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_EML_P02', '이메일 완료', '/path/email/complete', 'pth/eml/PTH_EML_P02.jsx', FALSE, 'BLOCK', NULL, 26),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_CRD_P01', '법인카드 1', '/path/card/1', 'pth/crd/PTH_CRD_P01.jsx', FALSE, 'BLOCK', NULL, 27),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_CRD_P02', '법인카드 2', '/path/card/2', 'pth/crd/PTH_CRD_P02.jsx', FALSE, 'BLOCK', NULL, 28),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_CRD_P03', '법인카드 3', '/path/card/3', 'pth/crd/PTH_CRD_P03.jsx', FALSE, 'BLOCK', NULL, 29),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_CRD_P04', '법인카드 4', '/path/card/4', 'pth/crd/PTH_CRD_P04.jsx', FALSE, 'BLOCK', NULL, 30),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_CRD_P05', '법인카드 5', '/path/card/5', 'pth/crd/PTH_CRD_P05.jsx', FALSE, 'BLOCK', NULL, 31),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_CRD_P06', '법인카드 6', '/path/card/6', 'pth/crd/PTH_CRD_P06.jsx', FALSE, 'BLOCK', NULL, 32),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_CRD_P07', '법인카드 7', '/path/card/7', 'pth/crd/PTH_CRD_P07.jsx', FALSE, 'BLOCK', NULL, 33),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_CRD_P08', '법인카드 완료', '/path/card/complete', 'pth/crd/PTH_CRD_P08.jsx', FALSE, 'BLOCK', NULL, 34),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_FOR_P01', '포레스트 1', '/path/forest/1', 'pth/for/PTH_FOR_P01.jsx', FALSE, 'BLOCK', NULL, 35),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_FOR_P02', '포레스트 2', '/path/forest/2', 'pth/for/PTH_FOR_P02.jsx', FALSE, 'BLOCK', NULL, 36),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_FOR_P03', '포레스트 3', '/path/forest/3', 'pth/for/PTH_FOR_P03.jsx', FALSE, 'BLOCK', NULL, 37),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_FOR_P04', '포레스트 4', '/path/forest/4', 'pth/for/PTH_FOR_P04.jsx', FALSE, 'BLOCK', NULL, 38),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_FOR_P05', '포레스트 5', '/path/forest/5', 'pth/for/PTH_FOR_P05.jsx', FALSE, 'BLOCK', NULL, 39),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_FOR_P06', '포레스트 6', '/path/forest/6', 'pth/for/PTH_FOR_P06.jsx', FALSE, 'BLOCK', NULL, 40),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_FOR_P07', '포레스트 완료', '/path/forest/complete', 'pth/for/PTH_FOR_P07.jsx', FALSE, 'BLOCK', NULL, 41),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_LEV_P01', '레벨업 1', '/path/level/1', 'pth/lev/PTH_LEV_P01.jsx', FALSE, 'BLOCK', NULL, 42),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_LEV_P02', '레벨업 2', '/path/level/2', 'pth/lev/PTH_LEV_P02.jsx', FALSE, 'BLOCK', NULL, 43),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_LEV_P03', '레벨업 3', '/path/level/3', 'pth/lev/PTH_LEV_P03.jsx', FALSE, 'BLOCK', NULL, 44),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_LEV_P04', '레벨업 4', '/path/level/4', 'pth/lev/PTH_LEV_P04.jsx', FALSE, 'BLOCK', NULL, 45),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_LEV_P05', '레벨업 5', '/path/level/5', 'pth/lev/PTH_LEV_P05.jsx', FALSE, 'BLOCK', NULL, 46),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_LEV_P06', '레벨업 완료', '/path/level/complete', 'pth/lev/PTH_LEV_P06.jsx', FALSE, 'BLOCK', NULL, 47),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_CPL_P01', '완주 입력', '/path/completion/input', 'pth/cpl/PTH_CPL_P01.jsx', FALSE, 'BLOCK', NULL, 48),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_CPL_P02', '완주 저장', '/path/completion/saved', 'pth/cpl/PTH_CPL_P02.jsx', FALSE, 'BLOCK', NULL, 49),
    ((SELECT task_id FROM task WHERE task_code = 'PTH'), 'PTH_CPL_P03', '완주 리포트', '/path/completion/report', 'pth/cpl/PTH_CPL_P03.jsx', FALSE, 'BLOCK', NULL, 50),

    ((SELECT task_id FROM task WHERE task_code = 'WIK'), 'WIK_INT_P01', '핑거 위키 인트로', '/wiki', 'wik/WIK_INT_P01.jsx', FALSE, 'BLOCK', NULL, 1),
    ((SELECT task_id FROM task WHERE task_code = 'WIK'), 'WIK_LIS_P01', '핑거 위키 목록', '/wiki/list', 'wik/WIK_LIS_P01.jsx', FALSE, 'BLOCK', NULL, 2),

    ((SELECT task_id FROM task WHERE task_code = 'ADM'), 'ADM_EMP_P01', '입사자 관리', '/admin/employees', 'adm/ADM_EMP_P01.jsx', FALSE, 'BLOCK', NULL, 1),
    ((SELECT task_id FROM task WHERE task_code = 'ADM'), 'ADM_WIK_P01', '위키 관리', '/admin/wiki', 'adm/ADM_WIK_P01.jsx', FALSE, 'BLOCK', NULL, 2),
    ((SELECT task_id FROM task WHERE task_code = 'ADM'), 'ADM_PRG_P01', '진행현황 관리', '/admin/progress', 'adm/ADM_PRG_P01.jsx', FALSE, 'BLOCK', NULL, 3),
    ((SELECT task_id FROM task WHERE task_code = 'ADM'), 'ADM_HRC_P01', 'HR 관리', '/admin/hr', 'adm/ADM_HRC_P01.jsx', FALSE, 'BLOCK', NULL, 4),

    ((SELECT task_id FROM task WHERE task_code = 'CHT'), 'CHT_MAN_F01', 'AI 챗봇', '/chat', 'cht/CHT_MAN_F01.jsx', FALSE, 'BLOCK', NULL, 1)
ON CONFLICT (screen_code) DO UPDATE SET
    task_id = EXCLUDED.task_id,
    screen_name = EXCLUDED.screen_name,
    route_path = EXCLUDED.route_path,
    file_path = EXCLUDED.file_path,
    login_required = EXCLUDED.login_required,
    back_action = EXCLUDED.back_action,
    back_screen_code = EXCLUDED.back_screen_code,
    display_order = EXCLUDED.display_order;

-- 징검다리 퀴즈 9문항 (AI 완주 리포트 생성 시 필요한 9개 응답의 근거)
INSERT INTO quiz (quiz_id, question, option1, option2, option3, option4, display_order) VALUES
    (1, '출근 후 가장 먼저 하는 것은?', '커피 마시기', '메일 확인', '일정 확인', '동료와 대화', 1),
    (2, '커피 취향은?', '아메리카노', '라떼', '에스프레소', '안 마심', 2),
    (3, '점심 스타일은?', '혼밥', '같이 먹기', '배달', '도시락', 3),
    (4, '휴식 스타일은?', '산책', '낮잠', '수다', '폰 보기', 4),
    (5, '여행 스타일은?', '즉흥형', '계획형', '반반', '안 감', 5),
    (6, '협업 스타일은?', '바로 이야기', '생각 후 이야기', '메신저', '회의', 6),
    (7, '친해지는 법은?', '먼저 걸어요', '기다려요', '활동으로', '천천히', 7),
    (8, '맵기 레벨은?', '순한맛', '중간맛', '매운맛', '불맛', 8),
    (9, '출근길엔?', '음악', '팟캐스트', '조용히', '통화', 9)
ON CONFLICT (quiz_id) DO NOTHING;

SELECT setval('quiz_quiz_id_seq', (SELECT MAX(quiz_id) FROM quiz));

-- 테스트용 : 김신입(employee_id=1)의 퀴즈 응답 9개 + 3행시 (AI 완주 리포트 생성 데모용)
INSERT INTO quiz_response (employee_id, quiz_id, selected_option, answered_at) VALUES
    (1, 1, '메일 확인', now()),
    (1, 2, '아메리카노', now()),
    (1, 3, '같이 먹기', now()),
    (1, 4, '산책', now()),
    (1, 5, '즉흥형', now()),
    (1, 6, '바로 이야기', now()),
    (1, 7, '먼저 걸어요', now()),
    (1, 8, '매운맛', now()),
    (1, 9, '음악', now())
ON CONFLICT (employee_id, quiz_id) DO NOTHING;

INSERT INTO three_line_poem (poem_id, employee_id) VALUES (1, 1) ON CONFLICT (poem_id) DO NOTHING;

INSERT INTO three_line_poem_line (poem_id, letter, text, line_order) VALUES
    (1, '김', '김빠지지 않게 즉흥적으로', 0),
    (1, '신', '신나는 도전을 즐기며', 1),
    (1, '입', '입사 후 멋지게 성장하겠습니다', 2)
ON CONFLICT (poem_id, line_order) DO NOTHING;

-- 테스트용 : 이신입(employee_id=2)도 동일하게 세팅 (실제 AI 연동 재검증용 — 1번은 이미 리포트 생성됨)
INSERT INTO quiz_response (employee_id, quiz_id, selected_option, answered_at) VALUES
    (2, 1, '일정 확인', now()),
    (2, 2, '라떼', now()),
    (2, 3, '혼밥', now()),
    (2, 4, '낮잠', now()),
    (2, 5, '계획형', now()),
    (2, 6, '생각 후 이야기', now()),
    (2, 7, '기다려요', now()),
    (2, 8, '순한맛', now()),
    (2, 9, '팟캐스트', now())
ON CONFLICT (employee_id, quiz_id) DO NOTHING;

INSERT INTO three_line_poem (poem_id, employee_id) VALUES (2, 2) ON CONFLICT (poem_id) DO NOTHING;

INSERT INTO three_line_poem_line (poem_id, letter, text, line_order) VALUES
    (2, '이', '이번 기회에 차근차근 배워서', 0),
    (2, '신', '신중하게 하나씩 익히고', 1),
    (2, '입', '입지를 단단히 다지겠습니다', 2)
ON CONFLICT (poem_id, line_order) DO NOTHING;

-- 테스트용 : 최준비(employee_id=4) — 아직 리포트 생성 안 한 상태로 남겨둠 (프론트 데모/발표용 라이브 생성 케이스)
INSERT INTO quiz_response (employee_id, quiz_id, selected_option, answered_at) VALUES
    (4, 1, '동료와 대화', now()),
    (4, 2, '에스프레소', now()),
    (4, 3, '배달', now()),
    (4, 4, '수다', now()),
    (4, 5, '반반', now()),
    (4, 6, '메신저', now()),
    (4, 7, '활동으로', now()),
    (4, 8, '불맛', now()),
    (4, 9, '통화', now())
ON CONFLICT (employee_id, quiz_id) DO NOTHING;

INSERT INTO three_line_poem (poem_id, employee_id) VALUES (3, 4) ON CONFLICT (poem_id) DO NOTHING;

INSERT INTO three_line_poem_line (poem_id, letter, text, line_order) VALUES
    (3, '최', '최선을 다해 부딪히고', 0),
    (3, '준', '준비된 자세로 배우며', 1),
    (3, '비', '비상하듯 성장하겠습니다', 2)
ON CONFLICT (poem_id, line_order) DO NOTHING;

-- 테스트용 : 정발표(employee_id=5) — 발표/데모 때 라이브 생성 보여주기용 여분
INSERT INTO quiz_response (employee_id, quiz_id, selected_option, answered_at) VALUES
    (5, 1, '커피 마시기', now()),
    (5, 2, '아메리카노', now()),
    (5, 3, '도시락', now()),
    (5, 4, '폰 보기', now()),
    (5, 5, '즉흥형', now()),
    (5, 6, '바로 이야기', now()),
    (5, 7, '먼저 걸어요', now()),
    (5, 8, '중간맛', now()),
    (5, 9, '음악', now())
ON CONFLICT (employee_id, quiz_id) DO NOTHING;

INSERT INTO three_line_poem (poem_id, employee_id) VALUES (4, 5) ON CONFLICT (poem_id) DO NOTHING;

INSERT INTO three_line_poem_line (poem_id, letter, text, line_order) VALUES
    (4, '정', '정성을 다해 배우고', 0),
    (4, '발', '발맞춰 함께 나아가며', 1),
    (4, '표', '표나게 성장하겠습니다', 2)
ON CONFLICT (poem_id, line_order) DO NOTHING;

-- 테스트용 : 한마무리(employee_id=6) — 근거 그리드(%) 기능 재검증용 여분
INSERT INTO quiz_response (employee_id, quiz_id, selected_option, answered_at) VALUES
    (6, 1, '메일 확인', now()),
    (6, 2, '아메리카노', now()),
    (6, 3, '같이 먹기', now()),
    (6, 4, '산책', now()),
    (6, 5, '계획형', now()),
    (6, 6, '바로 이야기', now()),
    (6, 7, '먼저 걸어요', now()),
    (6, 8, '매운맛', now()),
    (6, 9, '음악', now())
ON CONFLICT (employee_id, quiz_id) DO NOTHING;

INSERT INTO three_line_poem (poem_id, employee_id) VALUES (5, 6) ON CONFLICT (poem_id) DO NOTHING;

INSERT INTO three_line_poem_line (poem_id, letter, text, line_order) VALUES
    (5, '한', '한걸음씩 꾸준하게', 0),
    (5, '마', '마음을 다해 배우고', 1),
    (5, '무', '무엇이든 부딪혀보고', 2),
    (5, '리', '리듬을 찾아가겠습니다', 3)
ON CONFLICT (poem_id, line_order) DO NOTHING;

-- 테스트용 : 오검증(employee_id=7) — employeeName 필드 검증용 여분
INSERT INTO quiz_response (employee_id, quiz_id, selected_option, answered_at) VALUES
    (7, 1, '일정 확인', now()),
    (7, 2, '라떼', now()),
    (7, 3, '혼밥', now()),
    (7, 4, '낮잠', now()),
    (7, 5, '즉흥형', now()),
    (7, 6, '메신저', now()),
    (7, 7, '천천히', now()),
    (7, 8, '중간맛', now()),
    (7, 9, '조용히', now())
ON CONFLICT (employee_id, quiz_id) DO NOTHING;

INSERT INTO three_line_poem (poem_id, employee_id) VALUES (6, 7) ON CONFLICT (poem_id) DO NOTHING;

INSERT INTO three_line_poem_line (poem_id, letter, text, line_order) VALUES
    (6, '오', '오늘도 차근차근', 0),
    (6, '검', '검증하듯 확인하며', 1),
    (6, '증', '증명해 보이겠습니다', 2)
ON CONFLICT (poem_id, line_order) DO NOTHING;

SELECT setval('three_line_poem_poem_id_seq', (SELECT MAX(poem_id) FROM three_line_poem));
