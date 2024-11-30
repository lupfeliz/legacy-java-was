[TOC]
## 웹서비스 작성 규칙 및 유의사항

### 1. 제어흐름

```mermaid
sequenceDiagram
Actor User
User->>Frontend: 게시물 조회
Frontend->>atc01001: 게시물 조회 (atc01001a02)
atc01001->>DB: findOneByIdEquals(id)
DB->>atc01001: 게시물정보
atc01001->>Frontend: 게시물정보
Frontend->>User: 게시물 상세 페이지
```

### 2. 일반 웹페이지

### 3. REST 서비스 (POST / GET)

### 4. 설정접근

#### 4-1. 설정 reload

### 5. DB 연동

#### 5-1. mapper 작성규칙 및 유의사항

### 6. 타서비스 연동

#### 6-1. HTTP-CLIENT 사용