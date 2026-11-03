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

## 구현한 JDBC 라이브러리

- `JdbcTemplate.update(sql, args...)`: INSERT/UPDATE/DELETE 실행 및 영향받은 행 수 반환
- `JdbcTemplate.query(sql, rowMapper, args...)`: 각 행을 매핑한 목록 반환
- `JdbcTemplate.queryForObject(sql, rowMapper, args...)`: 결과가 없으면 null, 하나이면 객체, 여러 개이면 `DataAccessException`
- `RowMapper<T>`: ResultSet의 현재 행을 객체로 변환하는 콜백(행 번호는 0부터 시작)
- SQL 인자는 PreparedStatement로 바인딩하며, JDBC 자원은 try-with-resources로 반환합니다.
- SQLException은 원인을 보존한 `DataAccessException`으로 변환합니다.

UserDao와 UserHistoryDao에서 공통 JDBC 처리를 제거했고, 회원가입·로그인·사용자 조회는 UserDao를 사용합니다.

### DB 설정 및 검증

기본 DB는 실행 디렉터리의 `data/dashboard`에 저장되는 파일 기반 H2입니다.
재시작 시 기존 테이블과 데이터를 유지합니다. JVM 옵션 `-Djdbc.url=...`로 URL을 변경할 수 있습니다.
여러 서버에서 공유하려면 같은 H2 TCP 서버의 DB URL을 지정해야 합니다. 로컬 파일 DB만으로는 서로 다른 서버가 데이터를 공유하지 않습니다.
기존 메모리 저장소의 기본 계정은 자동 생성하지 않으므로 회원가입 후 로그인합니다.

```sh
./gradlew :app:test :jdbc:test :mvc:test
```

Java 21 이상이 필요합니다. 테스트에는 별도 메모리 DB와 임시 파일 DB를 사용하며, 실제 사용자 DB를 초기화하지 않습니다.
기존에 비활성화된 UserService 트랜잭션 테스트와 study 모듈의 학습 과제는 이번 JDBC 중복 제거 범위에 포함하지 않습니다.

### 2단계 리팩터링

조회와 수정은 내부 `execute` 템플릿을 공유하여 연결 생성, Statement 준비,
파라미터 설정, 예외 변환, 자원 반환을 한 곳에서 처리합니다.
조회 전용 ResultSet 순회는 조회 콜백에 두어 수정에 불필요한 매핑 구현을 요구하지 않습니다.

일반적인 인자는 기존 가변 인자 API를 사용하고, SQL 타입을 직접 지정해야 할 때는
`PreparedStatementSetter` 람다를 전달할 수 있습니다.

```java
jdbcTemplate.update("update users set email = ? where id = ?", statement -> {
    statement.setNull(1, java.sql.Types.VARCHAR);
    statement.setLong(2, userId);
});

List<User> users = jdbcTemplate.query(
        "select id, account, password, email from users where account = ?",
        statement -> statement.setString(1, account),
        userMapper);
```

`RowMapper<T>`와 `PreparedStatementSetter`는 SQLException을 선언할 수 있으며,
호출자는 원인을 보존하는 unchecked `DataAccessException`으로 실패를 받습니다.
인자가 없는 조회는 기존 `query(sql, rowMapper)`를 그대로 사용합니다.

### 리뷰 반영: 테스트 격리와 중복 가입 방지

- DAO/서비스 테스트는 `TestDataSource.create()`로 테스트마다 고유한 메모리 DB를 생성합니다.
  운영 `DataSourceConfig`나 `jdbc.url` 시스템 속성에 의존하지 않으므로 IDE에서 JUnit을
  직접 실행해도 회원 DB에 접근하지 않습니다. 기존 truncate도 제거했습니다.
- `users(account)` UNIQUE 인덱스로 동시 가입에도 같은 아이디가 하나만 저장됩니다.
  SQLState 23505는 `DuplicateKeyException`으로 변환하고 회원가입 화면은 HTTP 409와 안내를 반환합니다.
- 초기화 시 기존 테이블에도 인덱스를 추가합니다. 이미 중복 아이디가 있으면 초기화를 실패시키고
  원인을 보존합니다. 기존 회원을 자동 삭제하지 않으므로 중복 데이터는 별도 확인 후 정리해야 합니다.
