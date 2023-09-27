# JDBC 라이브러리 구현하기

개발자는 SQL 쿼리 작성, 쿼리에 전달할 인자, SELECT 구문일 경우
조회 결과를 추출하는 것만 집중할 수 있도록 라이브러리를 만들자.

## 힌트

- 리팩터링은 UserDaoTest를 활용해 진행한다.
- 중복을 제거하기 위한 라이브러리는 JdbcTemplate 클래스에 구현한다.
- DataSource는 DataSourceConfig 클래스의 getInstance() 메서드를 호출하면 된다.
