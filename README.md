# JDBC 라이브러리 구현하기  


- [X] UserDaoTest의 모든 테스트 케이스가 통과한다.  
- [X] UserDao가 아닌 JdbcTemplate 클래스에서 JDBC와 관련된 처리를 담당하고 있다.

- [ ] userDao와 userHistoryDao를 한 트랜잭션으로 묶으려면 동일한 Connection 객체를 사용하도록 변경하자.
- [ ] 스프링이 제공하는 PlatformTransactionManager 인터페이스를 활용하여 DAO가 Connection 객체를 파라미터로 전달받아 사용하지 않도록 만들어보자.
- [ ] 트랜잭션 롤백이 적용되어 UserServiceTest 클래스의 testTransactionRollback() 테스트 케이스가 통과한다.
- [ ] 트랜잭션 서비스와 애플리케이션 서비스가 분리되었다.