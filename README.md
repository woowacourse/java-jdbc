# JDBC 라이브러리 구현하기

### 1단계 - JDBC 라이브러리 구현하기

- [x] RowMapper 인터페이스를 추가한다.
- [x] JdbcTemplate으로 코드 이동시킨다.

### 2단계 - 리팩터링

- [x] PrepareStatement 생성하는 부분을 분리한다.
- [x] JdbcTemplate의 조회 시 중복을 제거한다.
- [x] Optional을 적용한다.
- [x] 단일 조회 시 결과가 2개 이상이라면 예외를 던지도록 수정한다.
- [ ] UserHistoryDao에 JdbcTemplate을 적용한다.
