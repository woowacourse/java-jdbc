# JDBC 라이브러리 구현하기

## 기능 요구사항 명세
- [x] 기존 UserDao 클래스 내 구현 메서드 중복로직을 JdbcTemplate으로 분리한다.
  - [x] UserDao 클래스에서는 쿼리 작성과 인자 전달만 수행하도록 한다.
  - [x] SELECT 구문을 수행하는 경우 조회 결과를 추출하는 것에만 집중하도록 한다.
- [x] UserDaoTest를 통과시킨다.
