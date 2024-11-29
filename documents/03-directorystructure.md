[TOC]

## 파일/디렉토리 구조 및 명명규칙

### 1. 전체 디렉토리 구조

<!--[-------------------------------------------------------------------------->
```bash
 ─┐
  ├── build.gradle [빌드파일]
  ├── gradlew [gradle실행파일]
  ├── gradlew.bat [gradle실행파일]
  ├── LICENSE.txt
  ├── README.md
  ├── settings.gradle [빌드파일]
  ├── documents [문서]
  │   ├── 02-environment.md
  │   ├── 03-directorystructure.md
  │   ├── 04-webservicedevelop.md
  │   ├── 05-webpagedevelop.md
  │   ├── 06-components.md
  │   ├── 07-functions.md
  │   └── 08-misc.md
  ├── gradle
  │   └── wrapper
  │       ├── gradle-8.2.1-bin.zip [파일추가필요]
  │       ├── gradle-wrapper.jar
  │       └── gradle-wrapper.properties
  ├── libs [외부라이브러리]
  │   └── tibero6-jdbc.jar
  └── src
      └── main
          ├── java
          │   └── com
          │       ├── github
          │       │   └── usingsky
          │       │       └── calendar
          │       │           └── KoreanLunarCalendar.java [음력캘린더]
          │       └── ntiple
          │           ├── Application.java [스프링부트메인]
          │           ├── ServletInitializer.java [war부트메인]
          │           ├── config
          │           │   ├── OpenAPIConfig.java [OPENAPI설정]
          │           │   ├── PersistentConfig.java [데이터베이스설정]
          │           │   ├── SecurityConfig.java [스프링보안설정]
          │           │   ├── TilesConfig.java [Tiles설정]
          │           │   └── WebResourceConfig.java [웹리소스설정]
          │           ├── system
          │           │   ├── CommonFilter.java [공통필터]
          │           │   ├── Constants.java [공통상수]
          │           │   ├── CustomTags.java [특수태그선언]
          │           │   ├── DbDateHandler.java [DB날자칼럼핸들링]
          │           │   ├── JSMinifier.java [자바스크립트난독화]
          │           │   ├── RequestAspect.java [웹요청Aspect]
          │           │   ├── RestResponse.java [Rest요청처리]
          │           │   ├── ScssWorker.java [스타일난독화]
          │           │   ├── Settings.java [공통설정]
          │           │   ├── SpreadSheetUtil.java [엑셀파일생성유틸]
          │           │   └── SystemException.java [공통오류]
          │           └── work
          │               ├── cmn
          │               │   ├── CommonEntity.java [공통DTO]
          │               │   ├── CommonRepository.java [공통DB처리기]
          │               │   └── CommonService.java [공통서비스]
          │               ├── smp
          │               │   ├── SampleController.java [웹요청컨트롤]
          │               │   ├── SampleRestController.java [REST요청컨트롤]
          │               │   └── SampleService.java [비즈니스로직서비스]
          │               └── sys
          │                   └── SystemRepository.java
          ├── resources
          │   ├── application.yml [설정파일]
          │   ... [프로파일별설정]
          │   ├── application-prod.yml
          │   ├── logback-spring.xml [로그설정]
          │   ├── mapper
          │   │   ├── common-repository.xml
          │   │   ... [데이터베이스map]
          │   │   └── system-repository.xml
          │   └── mybatis-config.xml
          └── webapp
              ├── assets
              │   ├── fonts
              │   │   ├── bootstrap-icons.woff
              │   │   ... [각종글꼴들]
              │   │   └── pretendard-semi-bold.woff2
              │   ├── libs
              │   │   ├── abortcontroller-polyfill-1.7.5.min.js
              │   │   ... [각종라이브러리]
              │   │   └── vue-3.5.12.min.js
              │   ├── scripts
              │   │   ├── components.js [컴포넌트선언]
              │   │   └── entry.js [함수선언]
              │   └── styles
              │       ├── components.scss [컴포넌트스타일]
              │       ├── globals.scss [공통스타일]
              │       └── util.scss [유틸성스타일]
              └── WEB-INF
                  ├── libs
                  │   ├── tag-page.tld [페이지태그]
                  │   └── tag-script.tld [스크립트태그]
                  ├── tiles.xml [타일즈선언]
                  └── views
                      ├── common
                      │   ├── aside.jsp [공통메뉴]
                      │   ├── assets-define.jsp [웹리소스임포트]
                      │   ├── dialog-container.jsp [레이어팝업]
                      │   ├── footer.jsp [꼬리말]
                      │   ├── header.jsp [머리말]
                      │   ├── init.jsp [페이지기초선언]
                      │   ├── launch-script.jsp [스크립트런처]
                      │   ├── layout.jsp [jsp레이아웃]
                      │   └── meta-define.jsp [html메타]
                      └── pages
                          ├── smp01001p01.jsp
                          ... [각종JSP파일]
                          └── smp01001s06.jsp
```
<!--]-------------------------------------------------------------------------->

### 2. Gradle / Spring 기본 및 환경파일

### 3. Gradle / Spring 기본파일
