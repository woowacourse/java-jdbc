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
