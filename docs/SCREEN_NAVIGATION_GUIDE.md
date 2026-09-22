# 화면정보 테이블 기반 프론트 라우트 연동 가이드

## 1. 목적

프론트 개발자가 화면 이동 경로를 각 페이지에서 임의로 관리하지 않고, 백엔드의 `task`, `screen_info` 테이블에 등록된 화면 코드와 route 정보를 기준으로 화면을 연결하기 위한 절차를 정의한다.

특히 뒤로가기는 브라우저 방문 기록에만 의존하지 않고, `screen_info`에 등록된 `backAction`, `backScreenCode`를 기준으로 처리한다. 이를 통해 이미 완료한 인증·퀴즈 등의 화면으로 다시 이동하여 업무 흐름이 꼬이는 문제를 방지한다.

---

## 2. 테이블 역할

### `task`

여러 화면을 하나의 업무 흐름으로 묶고, 업무의 시작 화면과 종료 후 이동할 화면을 정의한다.

| 컬럼 | 설명 | 예시 |
| --- | --- | --- |
| `taskCode` | 업무 식별 코드 | `ONBOARDING` |
| `taskName` | 업무 이름 | `온보딩` |
| `entryScreenCode` | 업무 시작 화면 코드 | `ONB-001` |
| `exitScreenCode` | 업무 종료 후 이동할 화면 코드 | `WIKI-001` |

### `screen_info`

각 화면의 코드, 프론트 route, 뒤로가기 규칙을 관리한다.

| 컬럼 | 설명 | 예시 |
| --- | --- | --- |
| `screenCode` | 화면 식별 코드 | `ONB-002` |
| `screenName` | 화면 이름 | `퀴즈 화면` |
| `routePath` | 프론트 route 경로 | `/onboarding/quiz` |
| `backAction` | 뒤로가기 동작 | `TARGET` |
| `backScreenCode` | 뒤로가기 대상 화면 코드 | `ONB-001` |
| `displayOrder` | 업무 내 화면 순서 | `2` |

---

## 3. 화면 추가 절차

### 1단계. 화면정보 확정

화면을 개발하기 전에 다음 값을 먼저 확정한다.

- 소속 업무 코드
- 화면 코드
- 화면 이름
- route 경로
- 뒤로가기 동작
- 뒤로가기 대상 화면 코드
- 업무 내 화면 순서

예시:

```text
업무 코드: ONBOARDING
화면 코드: ONB-002
화면 이름: 퀴즈
route: /onboarding/quiz
뒤로가기 동작: TARGET
뒤로가기 대상: ONB-001
순서: 2
```

### 2단계. 업무정보를 DB에 등록

동일한 `taskCode`가 이미 등록되어 있다면 이 단계는 생략한다.

```sql
INSERT INTO task (
    task_code,
    task_name,
    entry_screen_code,
    exit_screen_code
) VALUES (
    'ONBOARDING',
    '온보딩',
    'ONB-001',
    'WIKI-001'
);
```

### 3단계. 화면정보를 DB에 등록

```sql
INSERT INTO screen_info (
    task_id,
    screen_code,
    screen_name,
    route_path,
    back_action,
    back_screen_code,
    display_order
) VALUES (
    (SELECT task_id FROM task WHERE task_code = 'ONBOARDING'),
    'ONB-002',
    '퀴즈',
    '/onboarding/quiz',
    'TARGET',
    'ONB-001',
    2
);
```

### 4단계. 프론트 route 상수 등록

DB의 `routePath`와 동일한 경로를 프론트의 `routeConfig.js`에 등록한다.

```javascript
export const ROUTE_PATHS = {
  ONBOARDING_QUIZ: "/onboarding/quiz",
};
```

### 5단계. React Router에 화면 연결

```jsx
<Route
  path={ROUTE_PATHS.ONBOARDING_QUIZ}
  element={<QuizPage />}
/>
```

DB가 React 컴포넌트를 직접 실행하는 것은 아니다. DB에 등록된 경로와 프론트 라우터에 등록된 경로가 일치해야 해당 컴포넌트가 표시된다.

```text
DB의 routePath
    → AppRoutes의 path와 일치
    → React 화면 컴포넌트 표시
```

---

## 4. 뒤로가기 동작 규칙

### `TARGET`

`backScreenCode`로 지정된 화면의 route를 조회하여 이동한다.

```text
backAction: TARGET
backScreenCode: ONB-001
```

처리 흐름:

```text
ONB-001 화면정보 조회
→ ONB-001의 routePath 확인
→ 해당 route로 이동
```

### `EXIT`

현재 업무를 종료하고 `task.exitScreenCode`로 지정된 화면으로 이동한다.

```text
backAction: EXIT
backScreenCode: null
```

### `BLOCK`

완료 화면처럼 뒤로가기를 허용하지 않는다.

```text
backAction: BLOCK
backScreenCode: null
```

프론트에서는 뒤로가기 버튼을 숨기거나, 이동하지 않고 안내 메시지를 표시한다.

---

## 5. 프론트 뒤로가기 처리 흐름

```text
현재 화면 코드 확인
→ 백엔드에 화면 이동정보 요청
→ backAction 확인
→ TARGET: backScreenCode에 해당하는 route로 이동
→ EXIT: task의 exitScreenCode에 해당하는 route로 이동
→ BLOCK: 이동하지 않음
```

현재 프론트의 `useNavigation.js`는 브라우저 방문 기록을 사용하는 다음 방식으로 구현되어 있다.

```javascript
const goBack = () => {
  navigate(-1);
};
```

화면정보 API가 구현되면 다음과 같은 공통 함수로 변경한다.

```javascript
const goBackByScreenCode = async (screenCode) => {
  const response = await getScreenNavigation(screenCode);

  if (response.data.backAction === "BLOCK") {
    return;
  }

  navigate(response.data.targetRoutePath, {
    replace: true,
  });
};
```

`replace: true`를 사용하면 이동 전 화면이 브라우저 히스토리에 다시 쌓이는 것을 방지할 수 있다.

---

## 6. 화면정보 조회 API 초안

> 아래 API는 연동 방식 설명을 위한 초안이며 아직 구현되지 않았다. API 명세 확정 후 구현한다.

```http
POST /api/screens/navigation
```

요청:

```json
{
  "screenCode": "ONB-002"
}
```

응답:

```json
{
  "success": true,
  "data": {
    "screenCode": "ONB-002",
    "routePath": "/onboarding/quiz",
    "backAction": "TARGET",
    "targetScreenCode": "ONB-001",
    "targetRoutePath": "/onboarding/start"
  }
}
```

백엔드는 프론트가 화면정보를 두 번 조회하지 않도록 최종 이동 대상의 `targetRoutePath`까지 계산하여 반환한다.

---

## 7. 추가 구현이 필요한 백엔드 구성

현재 구현된 범위는 `Task`, `ScreenInfo`, `BackAction` 엔티티이다. 실제 프론트 연동을 위해 다음 구성이 추가로 필요하다.

- `TaskRepository`
- `ScreenInfoRepository`
- 화면 이동정보 조회 Service
- 화면 이동정보 조회 Controller
- 화면 이동정보 요청·응답 DTO
- 화면 코드 또는 대상 화면을 찾지 못했을 때의 오류 코드

---

## 8. 화면 개발자 작업 규칙

1. 화면을 개발하기 전에 화면 코드와 route를 확정한다.
2. 업무 및 화면정보를 DB에 먼저 등록한다.
3. 프론트 route 상수에 DB와 동일한 경로를 등록한다.
4. `AppRoutes`에서 route와 화면 컴포넌트를 연결한다.
5. 화면 이동은 공통 네비게이션 함수를 이용한다.
6. 각 페이지에서 `navigate(-1)`을 직접 호출하지 않는다.
7. DB의 `routePath`와 프론트 route가 정확히 일치하는지 확인한다.
8. `TARGET`일 때는 `backScreenCode`가 반드시 있어야 한다.
9. `EXIT`, `BLOCK`일 때는 `backScreenCode`를 비워 둔다.

---

## 9. 연동 확인 체크리스트

- [ ] `taskCode`가 중복되지 않았는가?
- [ ] `screenCode`가 중복되지 않았는가?
- [ ] `routePath`가 중복되지 않았는가?
- [ ] DB와 프론트의 route 경로가 일치하는가?
- [ ] `entryScreenCode`가 실제 화면 코드로 등록되어 있는가?
- [ ] `exitScreenCode`가 실제 화면 코드로 등록되어 있는가?
- [ ] `TARGET` 화면에 `backScreenCode`가 지정되어 있는가?
- [ ] `EXIT`, `BLOCK` 화면의 `backScreenCode`가 비어 있는가?
- [ ] 뒤로가기 시 브라우저 기록이 아니라 설정된 대상 화면으로 이동하는가?
- [ ] 완료 화면에서 이전 업무 화면으로 다시 진입하지 않는가?
