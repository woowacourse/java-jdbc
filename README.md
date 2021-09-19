# jwp-dashboard-jdbc

## 🚀 1단계 - JDBC 라이브러리 구현하기
## 🚀 2단계 - 리팩터링

- [x] UserDao의 update() 메서드 구현 및 insert(), update() 메서드 분리
- [x] UserDao의 insert(), update() 각 메서드를 InsertJdbcTemplate, UpdateJdbcTemplate 클래스로 분리
- [x] InsertJdbcTemplate, UpdateJdbcTemplate을 추상 클래스 JdbcTemplate으로 통합 및 추상화
- [x] JdbcTemplate의 User 객체에 대한 의존도 제거
- [x] UserDao의 select() 메서드를 SelectJdbcTemplate 클래스로 분리
- [x] SelectJdbcTemplate를 JdbcTemplate에 통합
- [x] 변수명 변경, UserDao의 findByAccount(), findAll() 메서드 구현
- [x] mapRow(), setValues() 메서드를 인터페이스로 분리
- [x] SQLException을 catch해서 Unchecked Custom Exception으로 다시 throw하도록 수정
- [x] 제네릭을 사용해 캐스팅 제거
- [x] 가변인자를 사용해 PreparedStatementSetter 인터페이스 구현의 번거로움 낮추기
- [x] 함수 분리 및 정리
- [x] DatabasePopulatorUtils 리팩터링
- [x] jdbc 패키지 jdbc 모듈로 이동


## 1차 코드리뷰 - 구구, 케빈

- [ ] PreparedStatementSetter의 default 메서드가 필요한 상황인지 고려해보기.
- [ ] JdbcTemplate을 사용하기 위해서 매번 구현하지 않도록 수정
  - [ ] 매번 DataSource 반환해야 하는 부분 제거
  - [ ] 싱글톤 빈 제공 고려
- [ ] 반복문을 라이브러리에 감출 방법 고려해 보기.
  - [ ] 사용자는 단순히 1개의 Row에서 데이터를 추출해 인스턴스화 하는 방법만 고려하도록 하기.
- [ ] `getUserSetWithResultValues(), getRowMapperForOnlyOneUserResult()` 메서드 분리의 필요성을 고민해 보기.
  - [ ] RowMapper 중복 사용 제거를 고민해 보기.
- [ ] `getUserMappedByResultSet()` JDBC 라이브러리로 옮기기.
- [ ] JdbcTemplate의 ResultSet을 try-with-resources로 처리하도록 수정.
