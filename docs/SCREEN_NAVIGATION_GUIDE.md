# 화면정보 테이블 기반 프론트 라우트 연동 가이드

## 1. 목적

프론트 화면의 URL과 뒤로가기 규칙을 페이지마다 작성하지 않고, 백엔드의 `task`, `screen_info` 테이블과 프론트 공통 유틸을 기준으로 관리한다.

- DB와 프론트 route 불일치 방지
- 완료한 인증·퀴즈 화면으로 재진입하는 문제 방지
- 페이지별 `navigate()` 직접 호출 방지
- 뒤로가기 시 전달 파라미터 유실 방지

## 2. 현재 구현 상태

### 백엔드

`Task`, `ScreenInfo`, `BackAction` 엔티티가 작성되어 있다. Repository, Service, Controller와 화면 목록 API는 아직 구현되지 않았다.

### 프론트엔드

| 파일 | 역할 | 현재 상태 |
| --- | --- | --- |
| `utils/screenConfig.js` | 화면 목록 조회·캐싱 | API 연동 대기 |
| `utils/historyStack.js` | route와 파라미터 기록 | 구현됨 |
| `hooks/useNavigation.js` | `goTo`, `goBack` 공통 처리 | DB 뒤로가기 규칙 미연동 |
| `config/routeConfig.js` | route 상수 관리 | 정적 방식 사용 중 |
| `routes/AppRoutes.jsx` | route와 컴포넌트 연결 | 정적 방식 사용 중 |
| `utils/apiError.js` | 오류 응답 변환 | `errorCode` 반영 필요 |

현재 `goBack()`은 `historyStack.pop()` 후 실제 이동에는 `navigate(-1)`을 사용한다.

## 3. 테이블 역할

### `task`

| 컬럼 | 설명 | 예시 |
| --- | --- | --- |
| `taskCode` | 업무 코드 | `ONBOARDING` |
| `taskName` | 업무 이름 | `온보딩` |
| `entryScreenCode` | 시작 화면 | `ONB-001` |
| `exitScreenCode` | 종료 후 이동 화면 | `WIKI-001` |

### `screen_info`

| 컬럼 | 설명 | 예시 |
| --- | --- | --- |
| `screenCode` | 화면 코드 | `ONB-002` |
| `screenName` | 화면 이름 | `퀴즈 화면` |
| `routePath` | 프론트 route | `/onboarding/quiz` |
| `backAction` | 뒤로가기 동작 | `TARGET` |
| `backScreenCode` | 뒤로가기 대상 | `ONB-001` |
| `displayOrder` | 화면 순서 | `2` |

## 4. 백엔드·프론트 계약 확정 사항

현재 프론트 임시 모델과 백엔드 모델의 이름이 다르므로 API 구현 전에 통일한다.

| 의미 | 프론트 임시 명칭 | 백엔드/권장 명칭 |
| --- | --- | --- |
| 화면 코드 | `screenId` | `screenCode` |
| route | `url` | `routePath` |
| 로그인 필요 여부 | `loginRequired` | 백엔드 컬럼 없음 |

이 문서에서는 `screenCode`, `routePath`를 사용한다. `loginRequired`는 테이블에 추가할지, `ProtectedRoute`에서 별도로 관리할지 회의에서 결정한다.

## 5. 화면 추가 절차

### 1단계. 화면정보 확정

업무 코드, 화면 코드, 화면 이름, route, 뒤로가기 동작·대상, 화면 순서를 확정한다.

```text
ONBOARDING / ONB-002 / 퀴즈 / /onboarding/quiz
TARGET / ONB-001 / 2
```

### 2단계. DB에 업무와 화면 등록

```sql
INSERT INTO task (task_code, task_name, entry_screen_code, exit_screen_code)
VALUES ('ONBOARDING', '온보딩', 'ONB-001', 'WIKI-001');

INSERT INTO screen_info (
    task_id, screen_code, screen_name, route_path,
    back_action, back_screen_code, display_order
) VALUES (
    (SELECT task_id FROM task WHERE task_code = 'ONBOARDING'),
    'ONB-002', '퀴즈', '/onboarding/quiz', 'TARGET', 'ONB-001', 2
);
```

이미 등록된 업무라면 `task` 등록은 생략한다.

### 3단계. React 컴포넌트 매핑

컴포넌트명은 DB에 저장하지 않으므로 프론트에 `screenCode → Component` 매핑이 필요하다.

```javascript
const SCREEN_COMPONENTS = {
  'ONB-001': OnboardingStartPage,
  'ONB-002': QuizPage,
  'WIKI-001': WikiPage,
};
```

```text
DB의 screenCode + routePath
→ 프론트의 screenCode + Component 매핑
→ React Router의 Route 생성
```

동적 라우팅 전환 전에는 `routeConfig.js`, `AppRoutes.jsx`에도 DB와 동일한 route를 등록한다.

## 6. 화면 목록 API 연동

프론트 `screenConfig.js`의 현재 임시 경로는 다음과 같다.

```http
POST /admin/screen/list
```

공통 prefix를 `/api`로 확정하면 프론트 경로도 `/api/admin/screen/list`로 함께 변경한다.

권장 응답:

```json
{
  "success": true,
  "data": [
    {
      "taskCode": "ONBOARDING",
      "screenCode": "ONB-002",
      "screenName": "퀴즈",
      "routePath": "/onboarding/quiz",
      "backAction": "TARGET",
      "backScreenCode": "ONB-001",
      "exitScreenCode": "WIKI-001",
      "displayOrder": 2
    }
  ]
}
```

`sendPost()`는 응답 전체를 반환하므로 `screenConfig.js`는 봉투의 `data`만 캐싱해야 한다.

```javascript
const response = await sendPost(SCREEN_LIST_API, {});

if (!response.success) {
  throw new Error(response.message);
}

cachedScreens = response.data;
```

화면 조회도 권장 명칭에 맞춘다.

```javascript
function findScreenByCode(screenCode) {
  return cachedScreens?.find(
    (screen) => screen.screenCode === screenCode
  ) ?? null;
}
```

## 7. 뒤로가기 동작

- `TARGET`: `backScreenCode` 화면의 `routePath`로 이동
- `EXIT`: 업무의 `exitScreenCode` 화면으로 이동
- `BLOCK`: 이동하지 않고 버튼을 숨기거나 안내 표시

`screen_info`는 업무상 이동 대상을 결정하고, `historyStack`은 이동 파라미터를 보관하며, React Router는 실제 route 이동을 담당한다.

```text
현재 screenCode 확인
→ screenConfig 캐시에서 화면정보 조회
→ backAction으로 대상 화면 결정
→ historyStack 파라미터 정리
→ 대상 routePath로 replace 이동
```

개념 예시:

```javascript
const goBackByScreenCode = (screenCode) => {
  const current = findScreenByCode(screenCode);

  if (!current || current.backAction === 'BLOCK') return;

  const targetCode = current.backAction === 'TARGET'
    ? current.backScreenCode
    : current.exitScreenCode;
  const target = findScreenByCode(targetCode);

  if (!target) {
    throw new Error('이동할 화면정보를 찾을 수 없습니다.');
  }

  pop();
  navigate(target.routePath, { replace: true });
};
```

현재 `goBack()`의 `navigate(-1)`은 연동 방식 확정 후 위 구조로 교체한다.

## 8. success 봉투와 오류 처리

HTTP 200이어도 `success:false`일 수 있으므로 프론트는 반드시 `success`를 확인한다. HTTP 200 응답은 Axios 오류 인터셉터로 들어가지 않는다.

현재 `apiError.js`는 `data.code`를 읽지만 백엔드는 `errorCode`를 사용하므로 다음처럼 변경해야 한다.

```javascript
return {
  errorCode: data.errorCode ?? null,
  message: data.message ?? '알 수 없는 오류가 발생했습니다.',
};
```

## 9. 추가 구현 항목

### 백엔드

- `TaskRepository`, `ScreenInfoRepository`
- 화면 목록 조회 Service·Controller·응답 DTO
- 초기 업무·화면 데이터 등록 방식
- 화면을 찾지 못한 경우의 오류 코드

### 프론트엔드

- `screenConfig.js`를 `screenCode`, `routePath`로 통일
- success 봉투의 `data` 배열 캐싱
- `screenCode → Component` 매핑
- 앱 시작 시 `fetchScreenList()` 호출
- `useNavigation.goBack()` DB 규칙 연동
- `apiError.js`의 `code`를 `errorCode`로 변경
- HTTP 200 + `success:false` 공통 처리

## 10. 개발 규칙 및 체크리스트

1. 화면 개발 전 화면 코드와 route를 확정하고 DB에 먼저 등록한다.
2. 프론트의 컴포넌트 매핑에도 화면을 등록한다.
3. 화면 이동은 `useNavigation`만 사용하고 페이지에서 `navigate()`를 직접 호출하지 않는다.
4. DB와 프론트의 route가 정확히 일치하는지 확인한다.
5. `TARGET`에는 `backScreenCode`를 지정하고 `EXIT`, `BLOCK`에서는 비워 둔다.
6. 모든 화면 코드에 대응하는 React 컴포넌트가 있는지 확인한다.
7. 앱 시작 시 화면 목록을 한 번 불러와 캐싱하는지 확인한다.
8. `response.data`만 목록으로 저장하고 `success:false`를 별도로 처리한다.
9. 뒤로가기 후 필요한 파라미터가 유지되는지 확인한다.
10. 완료 화면에서 이전 업무 화면으로 다시 진입하지 않는지 확인한다.
