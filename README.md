# ShopLab

Spring Boot와 React로 만드는 쇼핑몰 포트폴리오 프로젝트입니다.
사용자 쇼핑몰과 관리자 페이지를 구현하고, 선착순 타임딜의 **재고 동시성 문제** 해결이 핵심 과제입니다.

> 현재 개발 진행 중입니다.

## 구현 현황

- [x] 회원가입 (입력값 검증, BCrypt 비밀번호 암호화)
- [x] JWT 로그인, 내 정보 조회
- [x] 공통 응답 형식, 전역 예외 처리 (401·403 포함)
- [x] 회원가입·로그인 화면
- [x] Refresh Token (Redis, HttpOnly 쿠키, Rotation)
- [x] 상품·카테고리 (QueryDSL 동적 검색)
- [x] 장바구니
- [x] 주문·결제 (비관적 락 재고 차감, 토스페이먼츠 결제·환불, 미결제 주문 자동 취소)
- [ ] 타임딜 (재고 동시성 제어)
- [ ] 관리자 페이지
- [ ] 배포 (AWS, CI/CD)

## 기술 스택

| 구분     | 기술                                                                                                   |
| -------- | ------------------------------------------------------------------------------------------------------ |
| Backend  | Java 21, Spring Boot 4.1, Spring Data JPA, QueryDSL, Spring Security, JWT (jjwt), RestClient           |
| Frontend | React 19, TypeScript, Vite, React Router, TanStack Query, Zustand, React Hook Form + Zod, Tailwind CSS |
| Database | Oracle 21c XE, Redis 7                                                                                 |
| Payment  | 토스페이먼츠 결제위젯(주문서형) v2                                                                     |
| Infra    | Docker, Docker Compose                                                                                 |
| Test     | JUnit 5, Mockito                                                                                       |

## 프로젝트 구조

```
shoplab/
├── backend/             # Spring Boot API 서버
├── frontend/            # React 클라이언트
├── http/                # API 테스트 요청 (VS Code REST Client)
└── docker-compose.yml   # Redis
```

## 실행 방법

### 사전 준비

- JDK 21
- Node.js 22 이상
- Oracle Database 21c XE (서비스 이름 `XEPDB1`)
- Docker Desktop

### 결제 키 설정

토스페이먼츠 [개발자센터](https://developers.tosspayments.com/)의 **주문서형·결제창형 연동 키**(테스트용)를 아래 파일에 넣습니다. 두 파일 모두 Git에 포함되지 않습니다.

`frontend/.env.local`

```
VITE_TOSS_CLIENT_KEY=test_gck_...
```

`backend/src/main/resources/application-secret.yaml`

```yaml
toss:
  payments:
    secret-key: test_gsk_...
```

> 개인 프로젝트라 토스페이먼츠 문서용 공용 테스트 키로 연동했습니다. 테스트 키라 실제 결제는 일어나지 않습니다.

### 1. DB 계정 생성

`XEPDB1`에 `system` 계정으로 접속해 실행합니다.

```sql
CREATE USER shoplab IDENTIFIED BY shoplab1234
  DEFAULT TABLESPACE users
  QUOTA UNLIMITED ON users;

GRANT CONNECT, RESOURCE, CREATE VIEW TO shoplab;
```

### 2. Redis 실행

```bash
docker compose up -d
```

### 3. 백엔드 실행

```bash
cd backend
./mvnw spring-boot:run
```

`http://localhost:8080`에서 실행됩니다. 테이블은 JPA가 자동으로 생성합니다.

### 4. 프런트엔드 실행

```bash
cd frontend
npm install
npm run dev
```

`http://localhost:5173`에서 실행됩니다. `/api` 요청은 Vite 프록시를 통해 백엔드로 전달됩니다.

## 핵심 구현

### 주문 재고 동시성

- 주문 생성 시 상품 행을 `SELECT ... FOR UPDATE`(비관적 락)로 잠그고 재고 차감
- 여러 상품을 항상 id 순서로 잠가 데드락 방지, 락 대기 3초 초과 시 409 응답
- 주문 시점 가격·상품명을 스냅샷으로 저장

### 결제 (토스페이먼츠)

- 결제 요청 직전에 주문을 생성하고, 서버가 계산한 금액으로만 결제 진행
- 승인 시 서버에서 주문 금액과 대조해 **금액 위변조 차단**
- 외부 승인 API는 **트랜잭션 밖**에서 호출해 DB 커넥션·락 점유 방지
- 승인 후 DB 반영 실패 시 토스 결제를 자동 취소하는 **보상 트랜잭션**
- `Idempotency-Key`로 이중 결제 방지, 중복 승인 요청은 성공으로 응답
- 승인 응답 지연 시 주문번호로 결제 상태 재조회

### 미결제 주문 자동 취소

- 15분간 결제되지 않은 주문을 스케줄러가 취소하고 재고 복구
- 주문 행 락으로 "결제 승인"과 "자동 취소"가 동시에 일어나는 경쟁 상태 처리

### 타임딜 재고 동시성 비교

100개 한정 상품에 동시 요청 1,000건(스레드 32개)을 보내 방식별로 측정했습니다.

| 방법          | 판매 성공 | 남은 수량 | 소요 시간 | 결과                               |
| ------------- | --------- | --------- | --------- | ---------------------------------- |
| 락 없음       | 634       | 0         | 791 ms    | 534개 초과 판매 (갱신 손실)        |
| 비관적 락     | 100       | 0         | 960 ms    | 정확, 락 대기로 가장 느림          |
| 낙관적 락     | 100       | 0         | 562 ms    | 정확, 충돌 시 재시도               |
| 조건부 UPDATE | 100       | 0         | 171 ms    | 정확, 가장 빠름                    |
| Redis 선차감  | 100       | 0         | 401 ms    | 정확, DB 도달 요청 1,000건 → 100건 |

**선택: Redis 선차감 + 조건부 UPDATE**
요청 1,000건에서는 조건부 UPDATE가 가장 빨랐지만, 품절 요청까지 모두 DB에 도달합니다.
트래픽이 커질수록 DB 커넥션이 병목이 되므로, Redis `DECR`로 품절 요청을 앞단에서 걸러내고
통과한 요청만 조건부 UPDATE로 반영해 DB를 보호하면서 초과 판매도 이중으로 막았습니다.

## API

| Method | URL                                | 설명                                                | 인증 |
| ------ | ---------------------------------- | --------------------------------------------------- | ---- |
| GET    | `/api/health`                      | 서버 상태 확인                                      | -    |
| POST   | `/api/auth/signup`                 | 회원가입                                            | -    |
| POST   | `/api/auth/login`                  | 로그인 (Access Token 발급, Refresh Token 쿠키)      | -    |
| POST   | `/api/auth/reissue`                | 토큰 재발급 (Refresh Token 교체)                    | 쿠키 |
| POST   | `/api/auth/logout`                 | 로그아웃 (Refresh Token 삭제)                       | 쿠키 |
| GET    | `/api/members/me`                  | 내 정보 조회                                        | 필요 |
| GET    | `/api/categories`                  | 카테고리 트리                                       | -    |
| GET    | `/api/products`                    | 상품 목록 (카테고리·검색어·가격 필터, 정렬, 페이지) | -    |
| GET    | `/api/products/{id}`               | 상품 상세                                           | -    |
| GET    | `/api/cart`                        | 장바구니 목록·합계                                  | 필요 |
| GET    | `/api/cart/count`                  | 장바구니 상품 종류 수                               | 필요 |
| POST   | `/api/cart/items`                  | 장바구니 담기 (같은 상품은 수량 합치기)             | 필요 |
| PATCH  | `/api/cart/items/{id}`             | 수량 변경                                           | 필요 |
| DELETE | `/api/cart/items/{id}`             | 장바구니 상품 삭제                                  | 필요 |
| DELETE | `/api/cart/items?ids=1,2`          | 선택 삭제                                           | 필요 |
| POST   | `/api/orders`                      | 주문 생성 (재고 차감)                               | 필요 |
| GET    | `/api/orders?page=0`               | 내 주문 목록                                        | 필요 |
| GET    | `/api/orders/{orderNumber}`        | 주문 상세                                           | 필요 |
| POST   | `/api/orders/{orderNumber}/cancel` | 주문 취소 (결제 환불·재고 복구)                     | 필요 |
| POST   | `/api/payments/confirm`            | 결제 승인 (토스페이먼츠)                            | 필요 |
| GET    | `/api/time-deals`                  | 진행 중·예정 타임딜 목록                            | -    |
| GET    | `/api/time-deals/{id}`             | 타임딜 상세 (남은 수량)                             | -    |
| POST   | `/api/time-deals/{id}/orders`      | 타임딜 구매 (1인 1개)                               | 필요 |

## 트러블슈팅

### 결제 완료 화면이 나왔는데 주문이 '결제 대기'로 남는 문제

- **현상**: 토스 승인은 성공하고 화면도 결제 완료였지만, DB의 주문은 결제 대기·결제 기록 없음
- **원인**: 결제 반영 중 장바구니 삭제 쿼리의 `@Modifying(clearAutomatically = true)`가
  아직 DB에 쓰지 않은 변경(주문 상태 PAID, 결제 저장)이 남은 영속성 컨텍스트를 비워 변경이 유실됨
- **해결**: `flushAutomatically = true`를 함께 지정해 벌크 쿼리 실행 전에 변경을 먼저 반영
- **배운 점**: JPQL 벌크 쿼리는 영속성 컨텍스트를 거치지 않으므로 flush·clear 시점을 명확히 관리해야 함.
  운영 환경이라면 결제사와 DB를 비교하는 정합성 검증(대사) 배치가 필요

### QueryDSL 의존성 버전을 찾지 못하는 문제

- **원인**: `querydsl.version` 속성 이름이 Spring Boot가 관리하는 속성과 겹쳐, 원본 QueryDSL BOM 버전을 덮어씀
- **해결**: 속성 이름을 `openfeign-querydsl.version`으로 변경

### Spring Boot 4에서 `RestClient.Builder` 빈이 없는 문제

- **원인**: Spring Boot 4의 모듈 분리로 RestClient 자동 설정이 별도 스타터로 분리됨
- **해결**: `spring-boot-starter-restclient` 의존성 추가
