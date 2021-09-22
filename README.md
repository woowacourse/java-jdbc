# JDBC 라이브러리 구현하기

<br/>

## JDBC 라이브러리 구현하기

- 웹 서비스를 운영하려면 `데이터 영속성`이 있어야 함
- Java 진영에서는 어플리케이션의 DB 관련 처리를 위해 JDBC API 제공

<br/>

- [x] `UserDao` 구현 👉 `UserDaoTest` 활용하여 진행
- [ ] `JdbcTemplate` 구현 👉 중복을 제거하기 위한 라이브러리

<br/>

## 리팩토링

- [x] 메소드 추출
  - createQueryForInsert(), createQueryForUpdate()
  - setValuesForInsert(User, PreparedStatement), setValuesForUpdate(User, PreparedStatement)
- [x] 클래스 추출
  - `InsertJdbcTemplate`, `UpdateJdbcTemplate`
- [x] 템플릿 메소드 패턴 적용
  - `JdbcTemplate` (abstract)
- [x] 도메인 의존도 제거
- [x] 클래스 추출
  - `SelectJdbcTemplate`
- [x] 템플릿 메소드 패턴 적용
  - `JdbcTemplate` (abstract)
- [x] 불필요한 mapRow() 제거
  - `PreparedStatementSetter` (interface), `RowMapper` (interface)
- [ ] 라이브러리 확장
