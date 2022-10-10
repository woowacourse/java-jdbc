# JDBC 라이브러리 구현하기

## [🚀 1단계 - JDBC 라이브러리 구현하기](https://techcourse.woowahan.com/s/cCM7rQR9/ls/FQkSkTQC)

### 요구사항
클라이언트가 SQL 쿼리 작성, 쿼리에 전달할 인자, SELECT 구문일 경우 조회 결과를 추출하는 것만 집중할 수 있는 `JdbcTemplate` 구현

### 체크리스트
- [X] UserDaoTest의 모든 테스트 케이스가 통과한다.
- [X] UserDao가 아닌 JdbcTemplate 클래스에서 JDBC와 관련된 처리를 담당하고 있다.

## [🚀 3단계 - Transaction 적용하기](https://techcourse.woowahan.com/s/cCM7rQR9/ls/wB9LdcU2)

### 요구사항
- [ ] UserDao, UserHistoryDao가 한 트랜잭션을 공유하도록 한다.
- [ ] 트랜잭션 동기화를 통해 Dao가 Connection 객체를 parameter로 전달받지 않도록 한다.
- [ ] UserService를 추상화하여 트랜잭션 처리 로직을 분리한다.

### 체크리스트
- [ ] 트랜잭션 롤백이 적용되어 UserServiceTest 클래스의 testTransactionRollback() 테스트 케이스가 통과한다.
- [ ] 트랜잭션 서비스와 애플리케이션 서비스가 분리되었다.
