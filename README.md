# JDBC 라이브러리 구현하기
## 1단계 - JDBC 라이브러리 구현하기
- [x] UserDaoTest의 모든 테스트 케이스가 통과한다.
  - [x] update메서드를 구현한다.
  - [x] findAll메서드를 구현한다.
  - [x] findByAccount메서드를 구현한다.
- [x] UserDao가 아닌 JdbcTemplate 클래스에서 JDBC와 관련된 처리를 담당하고 있다.
  - [x] update메서드를 구현한다.
  - [x] query메서드를 구현한다.
  - [x] queryForObject메서드를 구현한다.


## 2단계 - 리팩터링
- DataAccessUtils분리
- final 키워드 추가

## 3단계 - Transaction 적용하기
- [x] 트랜잭션 롤백이 적용되어 UserServiceTest 클래스의 testTransactionRollback() 테스트 케이스가 통과한다.
- [x] 트랜잭션 서비스와 애플리케이션 서비스가 분리되었다.
