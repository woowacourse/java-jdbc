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
