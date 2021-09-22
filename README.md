# jwp-dashboard-jdbc

## 요구사항
- [x] 템플릿 콜백 패턴을 사용해서 JdbcTemplate 구현
  - [x] PreparedStatement를 connection을 이용해서 생성하는 메서드를 가지고 있는 PreparedStatementCreator 추가
  - [x] PreparedStatement 파라미터를 세팅해주는 PreparedStatementSetter 인터페이스와 구현체 추가
  - [x] ResultSet에 담긴 값을 추출해 객체를 생성하고 반환하는 RowMapper 추가
  - [x] RowMapper를 사용해 ResultSet으로 추출한 값을 리스트로 만들어서 반환하는 ResultSetExtractor 추가
  - [x] PreparedStatement를 사용해 각각 다른 로직을 주입하는 콜백 메서드가 있는 PreparedStatementCallback 추가
- [x] 구현한 JdbcTemplate 사용해서 UserDao 리팩토링
