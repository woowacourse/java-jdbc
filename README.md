# jwp-dashboard-jdbc

## TODO
- [x] 메서드 추출
  - [x] UserDao 클래스에서 insert 메서드를 참고해서 update 메서드를 구현한다.
  - [x] update와 insert 메서드에서 사용자가 수정해야 되는 부분을 메서드로 분리한다.
- [x] 클래스 추출
- [x] 템플릿 메서드 패턴 적용
  - [x] JdbcTemplate 추상 클래스로 만든다.
  - [x] 클래스명, 메서드명을 범용적으로 쓸 수 있게 수정한다.
  - [x] UserDao도 변경한 클래스를 사용하도록 변경한다.

- [x] 도메인 의존도 제거
- [x] 클래스 추출
- [x] 또 다른 템플릿 메서드 패턴 적용
- [x] 불필요한 mapRow 메서드 제거
- [x] PreparedStatementSetter 인터페이스 적용
- [ ] 라이브러리 확장
  - [x] JdbcTemplate이 datasource를 필드로 갖고 있도록 수정
  - [x] PreparedStatementSetter 람다 적용
  - [x] custom 예외를 사용하여 checked exception을 unchecked로 변경
  - [x] RowMapper 제네릭 활용
  - [x] try-with-resources 적용

- [x] findAll 구현
- [x] findByAccount 구현
- [ ] Controller in-memory user 저장 방식 dao 로 변경
