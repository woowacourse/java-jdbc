# JDBC 라이브러리 구현하기

## 1단계 요구사항 
- [x] UserDaoTest의 모든 테스트 케이스가 통과한다.
  - [x] UserDao의 update 메서드를 구현한다.
  - [x] UserDao의 findAll 메서드를 구현한다.
  - [x] UserDao의 findByAccount 메서드를 구현한다.
- [x] UserDao가 아닌 JdbcTemplate 클래스에서 JDBC와 관련된 처리를 담당하게한다.
  - [x] JdbcTemplate에서 DB 커넥션을 관리한다.
  - [x] JdbcTemplate이 Dao에서 실행할 sql 을 전달받아 실행시켜준다.
  - [x] update
  - [x] queryForObject
  - [x] query 

## 3단계 요구사항
- [ ] PlatformTransactionManager 를 활용해 Connection 객체를 보관하고 관리한다.
- [ ] 트랜잭션 롤백이 적용되어 UserServiceTest 클래스의 testTransactionRollback() 테스트 케이스가 통과한다.
- [ ] 트랜잭션 서비스와 애플리케이션 서비스가 분리되었다.
