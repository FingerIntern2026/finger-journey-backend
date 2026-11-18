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

INSERT INTO employee (employee_id, employee_no, name, organization_id, position_id, hire_date, birth_date, current_phase) VALUES
    (1, '12609001', '김신입', 2, 1, '2026-08-01', '2000-03-15', 'ONBOARDING'),
    (2, '12609002', '이신입', 2, 1, '2026-08-05', '2000-03-15', 'PREBOARDING'),
    (3, '12500001', '박기존', 1, 2, '2020-03-01', '2000-03-15', 'COMPLETED'),
    (4, '12609004', '최준비', 2, 1, '2026-09-01', '2000-03-15', 'ONBOARDING'),
    (8, '12609008', '윤체험', 2, 1, '2026-08-01', '2000-03-15', 'BAG')
ON CONFLICT (employee_id) DO NOTHING;

-- 발표용 직원은 단계별 진입 계정 3명, 진행/리포트 시연 계정 1명, 관리자 1명만 둔다.
-- 화면과 서버 모두 8자리 생년월일 사용: 20000315
-- 이신입 12609002: PREBOARDING → PB_LGN_P02
-- 윤체험 12609008: BAG → CHK_INT_P01
-- 김신입 12609001: ONBOARDING → PTH_MAP_P01, 오솔길 진행 현황 확인용
-- 최준비 12609004: ONBOARDING → PTH_MAP_P01, 라이브 리포트 생성용
-- 박기존 12500001: 관리자 역할 → WIK_INT_P01

-- 관리자 권한 검사(HR/ADMIN) 테스트용 계정: 박기존(사번 12500001)을 ADMIN으로 둔다.
-- admin_id는 자동 생성에 맡기고, 이미 있으면 건너뛴다.
INSERT INTO admin (employee_id, email, role) VALUES
    (3, 'admin@finger.local', 'ADMIN')
ON CONFLICT DO NOTHING;

-- 가방싸기 기본 체크 항목
-- check_code가 이미 있으면 이름과 노출 순서만 최신 값으로 맞춘다.
INSERT INTO bag_check_type (check_code, check_name, media_id, display_order) VALUES
    ('DOCUMENT', '서류 확인', NULL, 1),
    ('EQUIPMENT', '업무 장비 확인', NULL, 2),
    ('ACCOUNT', '계정 발급 확인', NULL, 3),
    ('CONTRACT', '계약서 작성', NULL, 4),
    ('SEAT', '좌석 배정 확인', NULL, 5)
ON CONFLICT (check_code) DO UPDATE SET
    check_name = EXCLUDED.check_name,
    display_order = EXCLUDED.display_order;


-- mock 데이터를 id를 직접 지정해서 넣었기 때문에,
-- 시퀀스(자동 증가 번호표 발급기)가 이 사실을 몰라 다음 등록 시 번호 충돌이 남
-- 그래서 mock 데이터 삽입 후 시퀀스를 현재 최대값 기준으로 재설정해줌
-- (organization/position은 지금 등록 API가 없어서 당장 안 터지지만, 나중에 생기면 employee와 똑같이 충돌하므로 미리 맞춰둠)
SELECT setval('organization_organization_id_seq', (SELECT MAX(organization_id) FROM organization));
SELECT setval('position_position_id_seq', (SELECT MAX(position_id) FROM position));
SELECT setval('employee_employee_id_seq', (SELECT MAX(employee_id) FROM employee));


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
-- 징검다리 퀴즈 9문항: 프론트 화면(quizData.js)의 문항·보기와 같은 내용과 순서로 맞춘다.
-- (음식 1~3, 휴식 4~6, 협업 7~9) 이미 들어 있는 문항도 같은 내용으로 갱신한다.
INSERT INTO quiz (quiz_id, question, option1, option2, option3, option4, display_order) VALUES
    (1, '어떤 커피를 좋아하시나요?', '아메리카노', '라떼', '달달한 음료', '커피 안 마셔요', 1),
    (2, '어떤 점심 메뉴를 좋아하시나요?', '한식', '양식', '중식', '일식', 2),
    (3, '어떤 맵기를 잘 드시나요?', '못 먹어요', '살짝 매운맛(신라면)', '매운맛(불닭)', '매운맛 러버', 3),
    (4, '쉬는 날을 어떻게 보내시나요?', '집에서 충전', '맛집 · 카페', '운동', '여행 · 나들이', 4),
    (5, '어떤 계절을 좋아하시나요?', '봄', '여름', '가을', '겨울', 5),
    (6, '여행은 어떻게 즐기시나요?', '계획형', '즉흥형', '맛집 중심', '휴양 중심', 6),
    (7, '어떻게 다가가면 가장 편하신가요?', '먼저 말 걸어주세요', '밥 같이 먹어요', '커피 좋아요', '천천히 친해져요', 7),
    (8, '출근길에는 주로 뭘 하시나요?', '음악', '유튜브 · 팟캐스트', '아무것도 안 들음', '자는 편', 8),
    (9, '함께 일할 때 어떤 방식이 편하신가요?', '바로 이야기하기', '메신저 선호', '같이 모여 공유하기', '먼저 생각한 뒤 이야기', 9)
ON CONFLICT (quiz_id) DO UPDATE SET
    question = EXCLUDED.question,
    option1 = EXCLUDED.option1,
    option2 = EXCLUDED.option2,
    option3 = EXCLUDED.option3,
    option4 = EXCLUDED.option4,
    display_order = EXCLUDED.display_order;

SELECT setval('quiz_quiz_id_seq', (SELECT MAX(quiz_id) FROM quiz));

-- 문항을 바꾸기 전에 저장된 응답(옛 보기 글자)을 같은 위치의 새 보기로 바꾼다.
-- 옛 보기 글자가 남아 있는 행만 바뀌고, 같은 문항 안에서 옛 보기와 새 보기가 겹치지 않아 여러 번 실행해도 결과가 같다.
UPDATE quiz_response response
SET selected_option = mapping.new_option
FROM (VALUES
    (1, '커피 마시기', '아메리카노'),
    (1, '메일 확인', '라떼'),
    (1, '일정 확인', '달달한 음료'),
    (1, '동료와 대화', '커피 안 마셔요'),
    (2, '아메리카노', '한식'),
    (2, '라떼', '양식'),
    (2, '에스프레소', '중식'),
    (2, '안 마심', '일식'),
    (3, '혼밥', '못 먹어요'),
    (3, '같이 먹기', '살짝 매운맛(신라면)'),
    (3, '배달', '매운맛(불닭)'),
    (3, '도시락', '매운맛 러버'),
    (4, '산책', '집에서 충전'),
    (4, '낮잠', '맛집 · 카페'),
    (4, '수다', '운동'),
    (4, '폰 보기', '여행 · 나들이'),
    (5, '즉흥형', '봄'),
    (5, '계획형', '여름'),
    (5, '반반', '가을'),
    (5, '안 감', '겨울'),
    (6, '바로 이야기', '계획형'),
    (6, '생각 후 이야기', '즉흥형'),
    (6, '메신저', '맛집 중심'),
    (6, '회의', '휴양 중심'),
    (7, '먼저 걸어요', '먼저 말 걸어주세요'),
    (7, '기다려요', '밥 같이 먹어요'),
    (7, '활동으로', '커피 좋아요'),
    (7, '천천히', '천천히 친해져요'),
    (8, '순한맛', '음악'),
    (8, '중간맛', '유튜브 · 팟캐스트'),
    (8, '매운맛', '아무것도 안 들음'),
    (8, '불맛', '자는 편'),
    (9, '음악', '바로 이야기하기'),
    (9, '팟캐스트', '메신저 선호'),
    (9, '조용히', '같이 모여 공유하기'),
    (9, '통화', '먼저 생각한 뒤 이야기')
) AS mapping(quiz_id, old_option, new_option)
WHERE response.quiz_id = mapping.quiz_id
  AND response.selected_option = mapping.old_option;

-- 테스트용 : 김신입(employee_id=1)의 퀴즈 응답 9개 + 3행시 (AI 완주 리포트 생성 데모용)
INSERT INTO quiz_response (employee_id, quiz_id, selected_option, answered_at) VALUES
    (1, 1, '라떼', now()),
    (1, 2, '한식', now()),
    (1, 3, '살짝 매운맛(신라면)', now()),
    (1, 4, '집에서 충전', now()),
    (1, 5, '봄', now()),
    (1, 6, '계획형', now()),
    (1, 7, '먼저 말 걸어주세요', now()),
    (1, 8, '아무것도 안 들음', now()),
    (1, 9, '바로 이야기하기', now())
ON CONFLICT (employee_id, quiz_id) DO NOTHING;

INSERT INTO three_line_poem (poem_id, employee_id) VALUES (1, 1) ON CONFLICT (poem_id) DO NOTHING;

INSERT INTO three_line_poem_line (poem_id, letter, text, line_order) VALUES
    (1, '김', '김빠지지 않게 즉흥적으로', 0),
    (1, '신', '신나는 도전을 즐기며', 1),
    (1, '입', '입사 후 멋지게 성장하겠습니다', 2)
ON CONFLICT (poem_id, line_order) DO NOTHING;

-- 이신입의 퀴즈·3행시 입력은 리포트 API 시연용으로 둔다.
INSERT INTO quiz_response (employee_id, quiz_id, selected_option, answered_at) VALUES
    (2, 1, '달달한 음료', now()),
    (2, 2, '양식', now()),
    (2, 3, '못 먹어요', now()),
    (2, 4, '맛집 · 카페', now()),
    (2, 5, '여름', now()),
    (2, 6, '즉흥형', now()),
    (2, 7, '밥 같이 먹어요', now()),
    (2, 8, '음악', now()),
    (2, 9, '메신저 선호', now())
ON CONFLICT (employee_id, quiz_id) DO NOTHING;

INSERT INTO three_line_poem (poem_id, employee_id) VALUES (2, 2) ON CONFLICT (poem_id) DO NOTHING;

INSERT INTO three_line_poem_line (poem_id, letter, text, line_order) VALUES
    (2, '이', '이번 기회에 차근차근 배워서', 0),
    (2, '신', '신중하게 하나씩 익히고', 1),
    (2, '입', '입지를 단단히 다지겠습니다', 2)
ON CONFLICT (poem_id, line_order) DO NOTHING;

-- 최준비는 퀴즈 응답과 3행시가 있지만 AI 리포트는 없어 라이브 생성 시연에 사용한다.
INSERT INTO quiz_response (employee_id, quiz_id, selected_option, answered_at) VALUES
    (4, 1, '커피 안 마셔요', now()),
    (4, 2, '중식', now()),
    (4, 3, '매운맛(불닭)', now()),
    (4, 4, '운동', now()),
    (4, 5, '가을', now()),
    (4, 6, '맛집 중심', now()),
    (4, 7, '커피 좋아요', now()),
    (4, 8, '자는 편', now()),
    (4, 9, '먼저 생각한 뒤 이야기', now())
ON CONFLICT (employee_id, quiz_id) DO NOTHING;

INSERT INTO three_line_poem (poem_id, employee_id) VALUES (3, 4) ON CONFLICT (poem_id) DO NOTHING;

INSERT INTO three_line_poem_line (poem_id, letter, text, line_order) VALUES
    (3, '최', '최선을 다해 부딪히고', 0),
    (3, '준', '준비된 자세로 배우며', 1),
    (3, '비', '비상하듯 성장하겠습니다', 2)
ON CONFLICT (poem_id, line_order) DO NOTHING;

SELECT setval('three_line_poem_poem_id_seq', (SELECT MAX(poem_id) FROM three_line_poem));

-- 핑거위키 기본 데이터: 프론트 위키 화면 목업(wikiCategories.js)과 같은 내용
-- id를 직접 지정해서 넣었기 때문에, 아래에서 시퀀스를 현재 최대값 기준으로 다시 맞춰준다.
-- 앱을 다시 실행해도 중복되지 않도록 ON CONFLICT DO NOTHING 처리 (관리자가 지운 기본 항목은 다음 실행 때 다시 생김)
INSERT INTO wiki_category (wiki_category_id, category_name, icon, display_order) VALUES
    (1, '회사생활 꿀팁', '🏠', 1),
    (2, '선배들의 꿀팁', '🎁', 2),
    (3, '회사 지도', '🗺️', 3),
    (4, '회사 주변 맛집', '🍜', 4),
    (5, '포레스트 & 레벨업', '🌲', 5)
ON CONFLICT (wiki_category_id) DO NOTHING;

INSERT INTO wiki_item (wiki_item_id, wiki_category_id, title, content, icon, display_order) VALUES
    (1, 1, '회의실 예약하는 법', '핑거 ERP의 예약 메뉴에서 회의실과 날짜·시간·인원을 선택해 예약해요. 30분 단위로 예약할 수 있어요.', '🗓️', 1),
    (2, 1, '복합기 사용법', '복합기 화면에서 사번으로 로그인한 뒤 출력, 복사, 스캔 중 필요한 기능을 선택해주세요.', '🖨️', 2),
    (3, 1, '택배 받는 법', '도착 알림을 확인하고 1층 수령 장소에서 본인 이름이 적힌 택배를 찾아주세요.', '📦', 3),
    (4, 1, '사내 와이파이 연결', '안내받은 사내 계정으로 와이파이에 연결한 뒤, 접속이 되지 않으면 관리자에게 문의해주세요.', '📶', 4),
    (5, 1, '연차·반차 신청 방법', 'HR 시스템에서 휴가 유형과 일정을 입력해 신청하고, 승인 상태를 확인해주세요.', '🌴', 5),
    (6, 1, '회식비 정산 방법', '회식비 영수증을 첨부한 뒤 정산 요청을 등록하면 담당자가 확인해요.', '🍴', 6),
    (7, 2, '업무일지는 그냥 바로 쓰는 게 편해요', '업무를 마친 직후 핵심만 기록해두면 나중에 회고와 인수인계가 훨씬 편해져요.', '🫧', 1),
    (8, 3, '회의실 위치 안내', '회의실은 층별 안내도에서 확인할 수 있으며, 예약 화면에서도 위치를 함께 확인할 수 있어요.', '📍', 1),
    (9, 4, '점심 맛집 추천', '도보 거리와 대기 시간을 고려한 팀원 추천 맛집을 확인해보세요.', '🥢', 1),
    (10, 5, '포레스트(Forest)가 뭔가요?', '포레스트는 회사 생활에 필요한 지식과 문화를 배우고, 나만의 여정을 완성해가는 성장 공간이에요.', '🌲', 1),
    (11, 5, '포레스트와 레벨업은 무슨 관계예요?', '포레스트에서 항목을 완료할수록 여정 진행도가 쌓이고 다음 레벨의 콘텐츠를 만날 수 있어요.', '🔗', 2)
ON CONFLICT (wiki_item_id) DO NOTHING;

SELECT setval('wiki_category_wiki_category_id_seq', (SELECT MAX(wiki_category_id) FROM wiki_category));
SELECT setval('wiki_item_wiki_item_id_seq', (SELECT MAX(wiki_item_id) FROM wiki_item));

-- 온보딩 오솔길 기본 스테이지
-- 프론트 pathMapData.js의 실제 콘텐츠 스테이지 8개와 순서를 맞춘다.
WITH stage_seed(stage_name, stage_order) AS (
    VALUES
        ('핑거 이야기', 1),
        ('취업규칙', 2),
        ('인사규정', 3),
        ('윤리규정', 4),
        ('이메일', 5),
        ('법인카드', 6),
        ('포레스트', 7),
        ('레벨업', 8)
)
INSERT INTO onboarding_stage (stage_name, stage_order)
SELECT seed.stage_name, seed.stage_order
FROM stage_seed seed
WHERE NOT EXISTS (
    SELECT 1
    FROM onboarding_stage stage
    WHERE stage.stage_order = seed.stage_order
);

-- 온보딩 진행 상태 시연용 기록: 김신입은 2단계 완료, 최준비는 첫 단계 진행 중.
-- 기존 진행 기록은 덮어쓰지 않고, 없는 사원-스테이지 조합만 추가한다.
WITH progress_seed(employee_id, stage_order, status, started_at, completed_at) AS (
    VALUES
        -- 김신입: 1~2단계 완료, 3단계 진행 중
        (1::BIGINT, 1, 'COMPLETED',  CURRENT_TIMESTAMP - INTERVAL '3 days', CURRENT_TIMESTAMP - INTERVAL '3 days' + INTERVAL '30 minutes'),
        (1::BIGINT, 2, 'COMPLETED',  CURRENT_TIMESTAMP - INTERVAL '2 days', CURRENT_TIMESTAMP - INTERVAL '2 days' + INTERVAL '30 minutes'),
        (1::BIGINT, 3, 'IN_PROGRESS', CURRENT_TIMESTAMP - INTERVAL '1 day',  NULL),

        -- 최준비: 첫 번째 단계 진행 중
        (4::BIGINT, 1, 'IN_PROGRESS', CURRENT_TIMESTAMP - INTERVAL '2 hours', NULL)
)
INSERT INTO employee_onboarding_stage (
    employee_id,
    onboarding_stage_id,
    status,
    started_at,
    completed_at,
    created_at,
    updated_at
)
SELECT
    seed.employee_id,
    stage.onboarding_stage_id,
    seed.status,
    seed.started_at,
    seed.completed_at,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM progress_seed seed
JOIN employee employee
    ON employee.employee_id = seed.employee_id
JOIN onboarding_stage stage
    ON stage.stage_order = seed.stage_order
WHERE NOT EXISTS (
    SELECT 1
    FROM employee_onboarding_stage progress
    WHERE progress.employee_id = seed.employee_id
      AND progress.onboarding_stage_id = stage.onboarding_stage_id
);

-- 체크인 비밀번호(전체 공통): HR이 관리자 화면에서 바꿀 수 있고, 서버를 다시 켜도 바꾼 값을 덮어쓰지 않는다.
INSERT INTO app_setting (setting_key, setting_value) VALUES
    ('CHECKIN_PASSWORD', 'finger')
ON CONFLICT DO NOTHING;
