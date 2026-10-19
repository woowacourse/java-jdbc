# 만들면서 배우는 스프링

## JDBC 라이브러리 구현하기

### 학습목표
- JDBC 라이브러리를 구현하는 경험을 함으로써 중복을 제거하는 연습을 한다.
- Transaction 적용을 위해 알아야할 개념을 이해한다.

### 시작 가이드
1. 이전 미션에서 진행한 코드를 사용하고 싶다면, 마이그레이션 작업을 진행합니다.
    - 학습 테스트는 강의 시간에 풀어봅시다.
2. LMS의 1단계 미션부터 진행합니다.

## 준비 사항
- 강의 시작 전에 docker를 설치해주세요.

## 학습 테스트
1. [ConnectionPool](study/src/test/java/connectionpool)
2. [Transaction](study/src/test/java/transaction)

## 웹서비스 DB 설정

앱은 기존 H2 드라이버를 사용하며, 기본 URL은 `jdbc:h2:file:./data/jdbc`입니다.
회원가입·로그인·사용자 조회는 UserDao와 JdbcTemplate을 통해 DB를 사용합니다.
앱 시작 시 기존 ContextLoaderListener가 테이블을 없을 때만 생성합니다.

- 실행 작업 디렉터리를 저장소 루트로 맞추면 데이터는 `data/jdbc.mv.db`에 저장됩니다.
- 같은 작업 디렉터리에서 재시작하면 기존 데이터가 유지됩니다. 기본 테스트 계정은 넣지 않으므로 먼저 회원가입합니다.
- JVM 옵션 `-Djdbc.url=jdbc:h2:file:/절대/경로/jdbc`로 저장 위치를 지정할 수 있습니다.
- Gradle 테스트는 별도의 H2 메모리 DB를 사용하므로 실행용 파일 DB를 초기화하지 않습니다. IDE에서 JUnit을 직접 실행한다면 VM 옵션 `-Djdbc.url=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;`을 지정합니다. 초기화 테스트는 메모리 DB가 아니면 실행을 중단합니다.
- 중복 계정 가입은 가입 화면으로 돌아가며 기존 계정을 변경하지 않습니다. DB에도 계정 유일성 제약을 둡니다.
- 이 파일 DB 구성은 로컬 단일 앱용입니다. 여러 앱 서버가 공유하려면 서버 방식 DB 구성이 필요합니다.

LMS 1단계에는 DB 제품 지정이 없어, 기존 H2 의존성을 유지하면서 재시작 후 보존이 가능한 파일 방식을 선택했습니다.
ConnectionPool 학습 테스트의 MySQL은 실험용 설정이며 웹서비스 DB와 별개입니다.
