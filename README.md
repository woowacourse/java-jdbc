# JDBC 라이브러리 구현하기

## 🚀 1단계 - JDBC 라이브러리 구현하기

- [x] UserDaoTest의 모든 테스트 케이스가 통과한다.
    - [x] insert를 완성한다
    - [x] update를 완성한다
    - [x] findAll을 완성한다
    - [x] findById를 완성한다
    - [x] findByAccount를 완성한다
- [x] UserDao가 아닌 JdbcTemplate 클래스에서 JDBC와 관련된 처리를 담당하고 있다.
    - [x] INSERT, UPDATE, DELETE 쿼리를 실행할 메서드를 만든다.
    - [x] SELECT 쿼리를 실행할 메서드를 만든다.

## 3단계 - Transaction 적용하

- [x] 트랜잭션 롤백이 적용되어 UserServiceTest 클래스의 testTransactionRollback() 테스트 케이스가 통과한다.
- [x] 트랜잭션 서비스와 애플리케이션 서비스가 분리되었다.