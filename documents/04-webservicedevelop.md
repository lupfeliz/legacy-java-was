[TOC]

[← 되돌아가기](../README.md)

## 웹서비스 작성 규칙 및 유의사항

### 1. 제어흐름

- 비즈니스로직 제어흐름 (페이지 및 API 호출)

```mermaid
sequenceDiagram
Actor User
participant APP
participant AuthFilter
participant CommonFilter
participant RequestAspect
participant Control
participant Service
participant Repository
User ->> APP: 요청
APP ->> AuthFilter: 권한 판단
AuthFilter ->> CommonFilter: 리소스 제어
CommonFilter ->> RequestAspect: 　
RequestAspect ->> RequestAspect: 선처리 (리퀘스트분석 등)
RequestAspect ->> Control: 　
Control ->> Service: 서비스 호출
Service ->> Repository: SQL 호출
Repository ->> Service: SQL 결과물
Service ->> RequestAspect: 서비스 결과물
RequestAspect ->> RequestAspect: 후처리 (오류 및 결과물 가공)
RequestAspect ->> APP: 　
APP ->>User: 결과물
```
- View(JSP) 처리흐름

```mermaid
sequenceDiagram
Actor User
participant Browser
participant Vue템플릿
participant APP
participant JSP
participant Tiles
participant Control
User ->> Browser: 페이지요청
Browser ->> APP: 　
APP ->> Control: 　
Control ->> Control: 서비스 호출
Control ->> Tiles: HTML 결과물 요청
Tiles ->> Tiles: 레이아웃
Tiles ->> JSP: 구성요소 요청
JSP ->> JSP: Model 데이터 직접 렌더
JSP ->> Tiles: 구성요소 HTML
Tiles ->> APP: HTML 결과물
APP ->> Browser: 페이지 렌더
Browser ->> Vue템플릿: 컴포넌트 렌더
Vue템플릿 ->> Browser: 렌더 결과
Browser ->>User: 페이지 최종 결과물
```

### 2. 일반 웹페이지 서비스

### 3. REST 서비스 (POST / GET)

### 4. 설정접근

#### 4-1. 설정 reload

### 5. DB 연동

#### 5-1. mapper 작성규칙 및 유의사항

### 6. 타서비스 연동

#### 6-1. HTTP-CLIENT 사용