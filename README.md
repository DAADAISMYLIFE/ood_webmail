# 객체지향설계 웹메일 프로젝트

## 프로젝트 소개

동의대학교 컴퓨터소프트웨어공학과 **객체지향설계** 웹메일 시스템 유지보수 프로젝트 입니다.

## 주요 기능
*   유저 추가/삭제
*   로그인/로그아웃
*   메일 읽기/쓰기/삭제
*   메일함 관리

## 개발 환경
*   **IDE:** NetBeans 23
*   **JAVA:** Zulu JDK 21

## 제임스 3.8.2 설치
프로젝트를 실행하기 위해서 James 3.8.2를 반드시 설치해야합니다.

<details>
    <summary>사용법</summary>

### 1. james 3.8.2 설치 및 확인
- [제임스 3.8.2 설치](https://www.apache.org/dyn/closer.lua/james/server/3.8.2/james-server-spring-app-3.8.2-app.zip) 에서 설치
- 압축 해제
- `bin` 폴더 경로 복사 해둠
- `cmd` 관리자 권한으로 실행 후 bin 경로로 이동
- `james.bat install` 입력
- `james.bat start` 입력


### 2. 초기 설정
- james 버전 3부터는 유저 ID가 email 형식으로 해야함
	- `<유저ID>@<도메인>`
1. 도메인 만들기
	1) bin 폴더 안에서 cmd나 powershell 실행
	2) `james-cli.bat adddomain <사용할 도메인>` 입력
2. 어드민 유저 생성
	1) `james-cli.bat adduser <사용할 유저 이메일> <비번>` 입력

### 3. jmx 설정
- 설치한 폴더 안에 `conf`폴더로 이동
- `jmxremote.password` 파일 열기
- 비밀번호 바꿔주기

### 4. webmail 시스템 파일 수정
- 교수님이 작성한 `webmail` 프로젝트의 `system.properties` 수정
- `root.id`는 `jmxremote.password`에 있던 `james-admin`
- `root.password`는 그 비번인 `admin`
- `admin.id`는 admin으로 추가한 유저 아이디로 수정
- `james.control.port`는 jmx 사용을 위한 9999로 수정
</details>

## 설치 및 실행 방법

1.  **저장소 클론:**
    ```bash
    git clone https://github.com/DAADAISMYLIFE/ood_webmail.git
    ```
2.  **NetBeans로 프로젝트 열기:** NetBeans 실행 후 `File ->Open Project`로 해당 프로젝트 열기

3.  **실행:** 프로젝트 오른쪽 클릭 후 `Run Maven -> Other Goals...` 클릭 후 `spring-boot:run`으로 실행

## 팀원

*   20212975 김민선
*   20183136 박경민
*   20203161 강순우
*   20212966 박소현
