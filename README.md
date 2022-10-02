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
- [ ] UserDao가 아닌 JdbcTemplate 클래스에서 JDBC와 관련된 처리를 담당하고 있다.
  - [x] UserDao 가 JdbcTemplate 에 의존하도록 수정한다.
  - [x] JdbcTemplate 에 query 메소드를 구현한다.
    - [x] query() 메소드는 List 형태로 반환한다.
    - [x] query() 메소드의 반환타입은 RowMapper 의 타입에 따라 결정된다.
    - [x] query() 메소드는 인자를 받을 수도 있고 안 받을 수도 있다.
  - [x] 단건 조회를 하는 queryForObject 메소드를 구현한다.
    - [x] DataAccessUtils.nullableSingleResult() 를 이용하여 반환 전에 데이터가 0 건인지 2건 이상인지를 검증한다.
