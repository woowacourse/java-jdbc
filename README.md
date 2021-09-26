# jwp-dashboard-jdbc

## TODO
- [ ] 메서드 추출
  - [ ] UserDao 클래스에서 insert 메서드를 참고해서 update 메서드를 구현한다.
  - [ ] update와 insert 메서드에서 사용자가 수정해야 되는 부분을 메서드로 분리한다.
- [ ] 클래스 추출
- [ ] 템플릿 메서드 패턴 적용
  - [ ] UpdateJdbcTemplate, InsertJdbcTemplate 클래스도 중복된 코드가 보인다. 클래스를 하나만 남기고 제거한다.
  - [ ] 남은 클래스를 추상 클래스로 만든다. 사용자가 구현해야되는 부분은 추상 메서드로 만든다.
  - [ ] 클래스명, 메서드명을 범용적으로 쓸 수 있게 수정한다.
  - [ ] UserDao도 변경한 클래스를 사용하도록 변경한다.
