## 1단계

### JdbcTemplate

- [x] JdbcTemplate에서 DataSource 관리

### Dao

- [x] SQL 쿼리 작성, 쿼리에 전달할 인자 설정하여 JdbcTemplate에 전달

## 2단계

### JdbcTemplate

- [x] SQLException을 Unchecked Exception로 변경

### Dao

- [x] 가변인자를 사용해서 prepareStatement 생성

## 3단계

- [x] UserServiceTest 테스트 통과
- [x] 트랜잭션 경계 설정
    - [x] userDao, userHistoryDao를 한 트랜잭션으로 묶기

## 4단계

- [ ] 트랜잭션 동기화(Transaction synchronization) 방식 적용
    - 트랜잭션을 시작하기 위한 Connection 객체를 따로 보관해두고, DAO에서 호출할 때 저장된 Connection을 가져다 사용
- [ ] DAO 파라미터에서 커넥션 제거
    - DataSourceUtils, TransactionSynchronizationManager 활용
- [ ] 트랜잭션 서비스 추상화
