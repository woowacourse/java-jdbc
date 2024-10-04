# JDBC 라이브러리 구현하기

## 1단계 - JDBC 라이브러리 구현하기

### 구현할 기능 목록

- [x] JDBC API를 이용한 DAO 로직을 구현한다.
    - [x] findAll 로직 구현
    - [x] update 구현
    - [x] 테스트 코드 작성
- [ ] DAO를 추상화한다.
- [ ] ResultSet을 객체로 변환하는 인터페이스 RowMapper를 구현한다.
    - [ ] Domain들의 RowMapper 구현
- [ ] 반복적인 DB 관련 작업을 대신 수행할 JdbcTemplate 구현하기
