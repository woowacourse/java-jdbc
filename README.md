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
