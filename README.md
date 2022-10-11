# JDBC 라이브러리 구현하기

## 1단계 JDBC 라이브러 구현하기

### 요구 사항

- [x] SQL 쿼리 작성, 쿼리 인자 등에 집중할 수 있도록 라이브러리 구현

### 체크리스트

- [x] UserDaoTest 통과
    - 테스트 목록
        - [x] findAll()
        - [x] findById()
        - [X] findByAccount()
        - [x] insert()
        - [x] update()
- [x] UserDAo가 아닌 JdbcTemplate 클래스에서 JDBC 관련 처리

## 3단계 Trasaction 적용하기

- [x] 트랜잭션 경계 설정
- [x] 트랜잭션 동기화 적용
- [x] 트랜잭션 서비스 추상화

### 체크리스트

- [x] 트랜잭션 롤백이 적용되어 UserServiceTest 클래스의 testTransactionRollback() 테스트 케이스가 통과한다.
- [x] 트랜잭션 서비스와 애플리케이션 서비스가 분리되었다.
