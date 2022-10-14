# JDBC 라이브러리 구현하기

## 기능 요구 사항
개발자는 SQL 쿼리 작성, 쿼리에 전달할 인자, SELECT 구문일 경우 조회 결과를 추출하는 것만 집중할 수 있도록 라이브러리를 만들자.

## 1단계

### 체크리스트
- [x] UserDaoTest의 모든 테스트 케이스가 통과한다.
- [x] UserDao가 아닌 JdbcTemplate 클래스에서 JDBC와 관련된 처리를 담당하고 있다.

### 구현한 기능
- UserDao 에서 Connection, Statement, ResultSet을 try-with-resources로 관리하도록 통일
- 모든 유저 조회
- 모든 유저 삭제
- id로 유저 조회
- 유저 정보 수정
- account로 유저 조회

## 2단계

### 체크리스트
- [x] `JdbcResourceHandler`는 데이터베이스와 통신하기 위한 자원 관리를 한다.`(Connection, Statement, ResultSet)`
- [x] `JdbcTemplate`은 `xxxDao`의 Sql 문을 받아 적절하게 쿼리를 실행시키고 ResultSet에서 데이터를 추출한다.
- [x] `JdbcTemplate`에서 예외가 발생하면 `DataAccessException`을 던진다.

## 3단계 - 트랜잭션 적용하기

### 체크리스트
- [x] 트랜잭션 롤백이 적용되어 UserServiceTest 클래스의 testTransactionRollback() 테스트 케이스가 통과한다.
- [x] 트랜잭션 서비스와 애플리케이션 서비스가 분리되었다.
