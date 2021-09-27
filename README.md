# jwp-dashboard-jdbc

- [x] UserDaoTest 통과하기
  - [x] update
  - [x] findAll
  - [x] deleteAll
  - [x] findByAccount

- [ ] JdbcTemplate으로 분리하기
  - [x] void를 리턴하는 update 메서드 분리하기
  - [x] Object, Collection을 리턴하는 query 메서드 분리하기
  - [ ] Template 및 Callback의 적용

- [x] 라이브러리 확장
  - [x] SQLException을 CustomException으로 변경하기
  - [x] RowMapper를 제네릭을 사용해서 구현하기
  - [x] PreparedStatementSetter를 가변 인자를 이용해서 구현하기
  - [x] 람다를 활용해서 코드량을 줄이기
  - [x] DataAccessUtils로 singleResult 가져오기

## 너잘 리뷰 반영
- [x] RowMapperResultSetExtractor의 개선
- [x] ArgumentPreparedStatementSetter의 개선
- [ ] JdbcTemplate 중복 개선

## 구구 리뷰 반영
- [x] JdbcTemplate에 delete 메서드
  - [x] update로 유지 
- [x] remove 2-depths
- [x] ArgumentPreparedStatementSetter 개선
