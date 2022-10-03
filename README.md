# JDBC 라이브러리 구현하기

## 기능 요구 사항
개발자는 SQL 쿼리 작성, 쿼리에 전달할 인자, SELECT 구문일 경우 조회 결과를 추출하는 것만 집중할 수 있도록 라이브러리를 만들자.

## 체크리스트
- [ ] UserDaoTest의 모든 테스트 케이스가 통과한다.
- [ ] UserDao가 아닌 JdbcTemplate 클래스에서 JDBC와 관련된 처리를 담당하고 있다.

## 구현한 기능
- UserDao 에서 Connection, Statement, ResultSet을 try-with-resources로 관리하도록 통일
- 모든 유저 조회 기능 구현
