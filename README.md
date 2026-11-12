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

## 미션 진행

<details>
<summary>Step 1 - JDBC 라이브러리 구현하기</summary>

- [x] UserDao에 사용자 저장, 조회, 수정 기능을 구현한다.
- [x] UserDaoTest를 통과시켜 리팩터링 전의 기본 동작을 확인한다.
- [x] JdbcTemplate에 SQL과 인자를 받아 저장, 수정 SQL을 실행하는 공통 기능을 구현한다.
- [x] UserDao의 insert (), update ()가 JdbcTemplate의 공통 기능을 사용하도록 변경한다.
- [x] 저장, 수정 공통화 후 UserDaoTest 5개가 통과하는지 확인한다.
- [x] JdbcTemplate에 SQL, 인자, 결과를 객체로 변환하는 방법을 받아 조회하는 공통 기능을 구현한다.
- [x] UserDao의 findById (), findByAccount (), findAll ()이 JdbcTemplate의 공통 기능을 사용하도록 변경한다.
- [x] JdbcTemplate에서 사용한 Connection, PreparedStatement, ResultSet을 정리한다.
- [x] UserDaoTest를 다시 통과시켜 리팩터링 후에도 기존 동작이 유지되는지 확인한다.
- [x] 여러 건 조회, 빈 결과, JdbcTemplate 주입 생성자를 검증한다.
- [x] SQL 실행과 결과 매핑 실패 시 원인 예외 유지와 자원 정리를 검증한다.

#### 구현 내용

- `update(sql, params)`가 저장, 수정 등 변경 SQL을 실행한다.
- `query(sql, params, rowMapper)`가 각 행을 변환해 목록으로 반환한다.
- `queryForObject(sql, params, rowMapper)`는 정확히 1건일 때 결과를 반환한다. 0건 또는 2건 이상이면 예상 건수와 실제 건수를 담은 `IllegalStateException`을
  발생시킨다.
- UserHistoryDao의 이력 저장도 `update()`를 사용하며, DataSource 또는 JdbcTemplate을 주입받을 수 있다.
- UserDao는 SQL, 인자, ResultSet에서 User로 변환하는 방법만 담당한다.
- JdbcTemplate은 연결, 인자 설정, SQL 실행, 결과 순회, 예외 처리, 자원 정리를 담당한다.
- 현재는 `Object[]`와 Object를 반환하는 RowMapper를 사용한다. 제네릭, 가변 인자, 람다 등의 개선은 2단계에서 진행한다.

#### 최종 검증

- UserDaoTest 10개, UserHistoryDaoTest 2개, JdbcTemplateTest 9개로 총 21개 통과.
- 단건 조회의 0건, 1건, 여러 건 처리와 자원 정리, 이력 DAO의 두 생성자 및 전체 저장 필드를 검증한다.
- UserDaoTest는 각 테스트 전에 사용자 테이블과 ID를 초기화하여 실행 순서에 의존하지 않는다.
- 3단계 대상인 UserServiceTest 2개는 기존 `@Disabled` 상태를 유지한다.
- study 모듈의 커넥션 풀, 트랜잭션, AOP 실습은 이번 검증과 제출 범위에서 제외한다.

</details>

<details>
<summary>Step 2 - 리팩터링</summary>

저장과 조회 동작을 유지하면서,
JdbcTemplate을 더 편하고 안전하게 사용할 수 있도록 개선한다.

- [X] 조회 결과를 원하는 타입으로 받아 캐스팅을 제거한다.
- [x] SQL 인자를 배열 생성 없이 전달할 수 있도록 개선한다.
- [x] 행을 객체로 변환하는 코드를 람다로 간결하게 표현한다.
- [x] DB 작업 실패를 커스텀 unchecked 예외로 전달한다.
- [x] try-with-resources로 JDBC 자원을 자동으로 정리한다.

#### 리뷰 반영

- [x] UserDao에서 호출되지 않는 쿼리 생성, 값 설정 메서드와 PreparedStatement import를 제거한다.
- [x] 작업 예외와 자원 종료 예외가 함께 발생하면 작업 예외를 원인으로 유지하고 종료 예외를 suppressed로 보존하는지 검증한다.
- [x] 연결 획득, PreparedStatement 생성, 인자 설정, 로깅, 예외 변환을 공통 실행 메서드로 모으고 달라지는 실행과 결과 처리만 콜백으로 전달한다.

</details>

<details>
<summary>Step 3 - Transaction 적용하기</summary>

- [x] UserServiceTest의 `@Disabled`를 제거하고 성공과 롤백 테스트를 실행한다.
- [x] JdbcTemplate에서 전달받은 Connection으로 조회와 변경 SQL을 실행한다.
- [x] 전달받은 Connection은 호출자가 종료하고, JdbcTemplate은 Statement와 ResultSet만 정리한다.
- [x] UserDao의 사용자 조회와 수정, UserHistoryDao의 이력 저장에 같은 Connection을 전달한다.
- [x] UserService에서 Connection을 얻고 `setAutoCommit(false)`로 트랜잭션 경계를 설정한다.
- [x] 비밀번호 변경과 변경 이력 저장이 모두 성공하면 커밋한다.
- [x] 작업 중 예외가 발생하면 롤백하여 기존 비밀번호와 이력 상태를 유지한다.
- [x] 커밋 또는 롤백 후 Connection을 종료하고, 롤백과 종료 실패 시 원래 예외를 보존한다.

#### 구현 내용

- `UserService(DataSource, UserDao, UserHistoryDao)`로 트랜잭션에 사용할 연결의 출처를 주입한다.
- `changePassword()`에서 연결 하나를 열어 조회, 비밀번호 수정, 이력 저장에 전달한다.
- 두 저장이 성공하면 커밋하고, SQL 예외나 런타임 예외, Error가 발생하면 롤백한다.
- 커밋 또는 롤백이 완료되면 기존 autoCommit 설정을 복구하고 연결을 종료한다.
- 롤백 자체가 실패하면 autoCommit 복구를 건너뛰어 미완료 작업의 암묵적 커밋을 피한다.
- 롤백과 연결 종료 실패는 최초 예외의 suppressed에 남긴다.

</details>

<details>
<summary>Step 4 - Transaction synchronization 적용하기</summary>

#### 기능 요구사항

- [x] TransactionSynchronizationManager에 스레드별로 DataSource와 Connection을 등록, 조회, 해제한다.
- [x] 같은 스레드와 DataSource에서는 같은 Connection을 사용하고, 다른 스레드의 연결은 분리한다.
- [x] 마지막 Connection을 해제하면 ThreadLocal을 정리한다.
- [x] DataSourceUtils에서 동기화된 Connection을 재사용하고, 동기화된 연결은 DAO 작업 후에도 열어 둔다.
- [x] 트랜잭션 밖에서 얻은 연결은 작업 종료 시 닫는다.
- [x] JdbcTemplate이 DataSourceUtils를 통해 연결을 얻고 반환하도록 변경한다.
- [x] DAO와 비즈니스 서비스 호출에서 Connection 매개변수를 제거한다.
- [x] findById, save, changePassword를 선언한 UserService 인터페이스를 정의하고 AppUserService와 TxUserService로 역할을 분리한다.
- [x] AppUserService는 사용자 조회, 비밀번호 변경, 이력 저장의 비즈니스 로직을 담당한다.
- [x] TxUserService는 연결 등록, 트랜잭션 시작, 커밋과 롤백, 연결 해제와 종료를 담당한다.
- [x] 성공 시 비밀번호와 이력을 함께 커밋하고, 실패 시 두 변경을 함께 롤백한다.
- [x] 트랜잭션 종료 후 연결 설정과 스레드 저장소가 정리되고, 정리 중 실패해도 최초 예외를 보존한다.

#### 구현 내용

- TxUserService가 DataSourceUtils로 연결을 얻고 TransactionSynchronizationManager에 등록한다.
  같은 스레드의 JdbcTemplate은 같은 DataSource에 등록된 연결을 재사용한다.
- DataSourceUtils는 일반 연결을 자동 등록하지 않는다. 트랜잭션에 속한 연결의 등록과 해제는 TxUserService가 담당한다.
- DataSourceUtils는 등록된 연결의 조기 종료를 막는다. 트랜잭션이 끝나면 먼저 unbindResource로 등록을 해제한 뒤 releaseConnection으로 닫는다.
- AppUserService와 DAO는 Connection을 전달받지 않고 기존 조회, 수정, 이력 저장 API를 사용한다.
- `new TxUserService(appUserService)`는 DataSourceConfig의 DataSource를 사용한다.
  별도의 DataSource가 필요하면 `new TxUserService(appUserService, dataSource)`로 주입한다.
- 커밋 또는 롤백 후 기존 autoCommit 설정을 복구한다. 롤백 자체가 실패하면 암묵적 커밋을 피하도록 복구를 건너뛰고 연결을 종료한다.
- 연결 획득과 종료 실패는 원인을 가진 DataAccessException 하위 예외로 전달하고, 작업 중 정리 실패는 최초 예외의 suppressed로 남긴다.

</details>
