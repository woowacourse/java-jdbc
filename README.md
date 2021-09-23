# jwp-dashboard-jdbc

- [x] UserDaoTest 통과하기
  - [x] update
  - [x] findAll
  - [x] deleteAll
  - [x] findByAccount

- [ ] JdbcTemplate으로 분리하기
  - [ ] 중복된 메서드를 추출해서 클래스로 분리하기 
  - [ ] Template 적용
  - [ ] Callback 적용

- [ ] 라이브러리 확장
  - [ ] SQLException을 CustomException으로 변경하기
  - [ ] RowMapper를 제네릭을 사용해서 구현하기
  - [ ] PreparedStatementSetter를 가변 인자를 이용해서 구현하기
  - [ ] 람다를 활용해서 코드량을 줄이기
