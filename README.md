# JDBC 라이브러리 구현하기

## step1
- [x] UserDaoTest의 모든 테스트 케이스가 통과한다.
- [x] UserDao가 아닌 JdbcTemplate 클래스에서 JDBC와 관련된 처리를 담당하도록 한다.

## step2 
- [x] SQLException은 Checked Exception이다. 커스텀 Exception을 추가해서 사용자는 Unchecked Exception이 되도록 변경 
- [x] UserDaoTest 격리
- [x] setValues를 인터페이스로 분리하기 
- [ ] JDBCTemplate test 짜면서 하기
- [x] queryForObject에서 결과 한 개 이상 또는 비었을 때 예외 처리

## step3
- [ ] 트랜잭션 롤백이 적용되어 UserServiceTest 클래스의 testTransactionRollback() 테스트 케이스가 통과하게 한다. 
- [ ] 트랜잭션 서비스와 애플리케이션 서비스 분리하기 
