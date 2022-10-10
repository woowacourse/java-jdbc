# JDBC 라이브러리 구현하기

---

## 1단계 기능 요구 사항

개발자는 SQL 쿼리 작성, 쿼리에 전달할 인자, SELECT 구문일 경우 조회 결과를 추출하는 것만 집중할 수 있도록 라이브러리를 만들자.

## 1단계 구현 내용 정리

- [x] UserDaoTest의 모든 테스트 케이스가 통과한다.
  - [x] account 로 일치하는 유저를 찾아 조회할 수 있다.
  - [x] user 전체를 조회할 수 있다.
  - [x] user 를 저장할 수 있다.
  - [x] user 정보를 수정할 수 있다.
  - [x] id 로 일치하는 유저를 찾아 조회할 수 있다.
- [x] UserDao가 아닌 JdbcTemplate 클래스에서 JDBC와 관련된 처리를 담당하고 있다.
  - [x] UserDao 가 JdbcTemplate 에 의존하도록 수정한다.
  - [x] JdbcTemplate 에 query 메소드를 구현한다.
    - [x] query() 메소드는 List 형태로 반환한다.
    - [x] query() 메소드의 반환타입은 RowMapper 의 타입에 따라 결정된다.
    - [x] query() 메소드는 인자를 받을 수도 있고 안 받을 수도 있다.
  - [x] 단건 조회를 하는 queryForObject 메소드를 구현한다.
    - [x] DataAccessUtils.nullableSingleResult() 를 이용하여 반환 전에 데이터가 0 건인지 2건 이상인지를 검증한다.
  - [x] update 메소드를 통해서 데이터를 변경할 수 있다.
    - [x] INSERT 문의 경우 데이터를 새롭게 생성해 저장한다.
    - [x] UPDATE 문의 경우 데이터를 변경한다.
    - [x] PreparedStatement 의 executeUpdate() 메소드를 통해 변경된 데이터의 row 수를 반환한다.

## 2단계 구현 내용 정리

- [x] Statement Setting 책임 분리
  - [x] 가변인자를 사용하도록 구현
- [x] JdbcTemplateTest 개선
  - [x] ResultSet도 함께 mocking 하여 구체적인 결과도 함께 테스트하도록 수정
- [x] RowMapper 에서 제네릭을 활용


## 3단계 구현 내용 정리
- [x] 트랜잭션 롤백이 적용되어 UserServiceTest 클래스의 testTransactionRollback() 테스트 케이스가 통과한다.
  - [x] changePassword() 에 대해 트랜잭션 롤백을 적용하여 예외가 발생하면 트랜잭션 시작 이전 상태로 되돌린다.
  - [x] JdbcTemplate은 Connection을 파라미터로 받아 update 할 수 있다.
- [x] 트랜잭션 동기화 적용
  - [x] 스프링에서 제공하는 PlatformTransactionManager 를 통해서 트랜잭션 동기화를 적용한다.
    - [x] 트랜잭션을 위한 Connection을 별도로 보관해두고 DAO에서 호출시 저장된 커넥션을 사용한다.
  - [x] 스프링을 통해서 트랜잭션을 관리하므로 JdbcTemplate 에서는 DataSourceUtils를 통해서 동일한 Connetion 획득
- [x] 트랜잭션 서비스와 애플리케이션 서비스가 분리되었다.
  - [x] UserService 인터페이스를 도출한다.
  - [x] 데이터 액세스를 처리하는 AppUserService를 분리한다.
  - [x] 트랜잭션 처리를 담당하는 TxUserService를 분리하고, AppUserService를 이용하여 서비스 로직을 처리한다.
  - [x] FunctionalInterface 를 활용하여 중복 로직을 제거한다.
