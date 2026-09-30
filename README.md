# ShopLab

Spring Boot와 React로 만드는 쇼핑몰 포트폴리오 프로젝트입니다.
사용자 쇼핑몰과 관리자 페이지를 구현하고, 선착순 타임딜의 **재고 동시성 문제** 해결이 핵심 과제 입니다.

> 현재 개발 진행 중입니다.

## 구현 현황

- [x] 회원가입 (입력값 검증, BCrypt 비밀번호 암호화)
- [x] JWT 로그인, 내 정보 조회
- [x] 공통 응답 형식, 전역 예외 처리 (401·403 포함)
- [x] 회원가입·로그인 화면
- [ ] Refresh Token (Redis, HttpOnly 쿠키)
- [ ] 상품·카테고리
- [ ] 장바구니·주문·결제
- [ ] 타임딜 (재고 동시성 제어)
- [ ] 관리자 페이지
- [ ] 배포 (AWS, CI/CD)

## 기술 스택

| 구분     | 기술                                                                                                   |
| -------- | ------------------------------------------------------------------------------------------------------ |
| Backend  | Java 21, Spring Boot 4.1, Spring Data JPA, Spring Security, JWT (jjwt)                                 |
| Frontend | React 19, TypeScript, Vite, React Router, TanStack Query, Zustand, React Hook Form + Zod, Tailwind CSS |
| Database | Oracle 21c XE, Redis 7                                                                                 |
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

## API

| Method | URL                | 설명                       | 인증 |
| ------ | ------------------ | -------------------------- | ---- |
| GET    | `/api/health`      | 서버 상태 확인             | -    |
| POST   | `/api/auth/signup` | 회원가입                   | -    |
| POST   | `/api/auth/login`  | 로그인 (Access Token 발급) | -    |
| GET    | `/api/members/me`  | 내 정보 조회               | 필요 |
