# JDBC 라이브러리 구현하기

### 1단계 요구사항 
- [x] UserDaoTest의 모든 테스트 케이스가 통과한다.
- [x] UserDao가 아닌 JdbcTemplate 클래스에서 JDBC와 관련된 처리를 담당하고 있다.

### 2단계 요구사항
- [x] 메서드 호출
- [x] 클래스 추출
- [x] 템플릿 메서드 패턴 적용
- [x] 도메인 의존도 제거

### 3단계 요구사항
- [ ] 트랜잭션 롤백이 적용되어 UserServiceTest 클래스의 testTransactionRollback() 테스트 케이스가 통과한다.
- [ ] 트랜잭션 서비스와 애플리케이션 서비스가 분리되었다.