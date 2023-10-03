# JDBC 라이브러리 구현하기

### 1단계 - JDBC 라이브러리 구현하기

- [x] RowMapper 인터페이스를 추가한다.
- [x] JdbcTemplate으로 코드 이동시킨다.

### 2단계 - 리팩터링

- [x] PrepareStatement 생성하는 부분을 분리한다.
- [x] JdbcTemplate의 조회 시 중복을 제거한다.
- [x] Optional을 적용한다.
- [x] 단일 조회 시 결과가 2개 이상이라면 예외를 던지도록 수정한다.
- [x] UserHistoryDao에 JdbcTemplate을 적용한다.

### 3단계 - Transaction 적용하기

- [ ] changePassword 메서드 내에서 동일한 Connection을 사용하도록 한다.
- [ ] TransactionTemplate을 만들어서 트랜잭션을 보장한다.
- [x] JdbcTemplate에서 단일 조회 결과가 0개면 빈 Optional을 반환하도록 수정한다.   
