# JDBC 라이브러리 구현하기
## 중복된 코드 제거하기
- [x] JdbcTemplate 구현하기
  - [x] 데이터를 넣는 메소드 구현
  - [x] 데이터를 조회하는 메소드 구현
    - [x] 데이터 단일 조회 메소드
    - [x] 데이터 여러개 조회 메소드
  - [x] 데이터를 수정하는 메소드 구현
## 리팩토링
- [x] PrepareStatementGenerator 생성하기
- [x] PrepareStatementSetter 만들기
- [x] result를 생성하는 객체 만들기
- [x] 객체의 응집성 높이기
- [x] 클래스 네이밍 포괄적으로 수정하기
## 트랜잭션 적용하기
- [x] UserHistoryDao jdbcTemplate 이용하게 수정
- [x] 트랜잭션 동기화하기
  - [x] jdbcTemplate에 connection을 받는 update메소드 오버라이드하기
