# 구현 기능 목록

## 1단계 JDBC 라이브러리 구현
- [x] UserDao에 있는 코드 JdbcTemplate으로 이동
- [x] UserDao 동작하도록 구현

## 2단계 
- [x] 예외처리 코드 작성
  - [x] 쿼리 조회 결과가 2개 이상이거나 없을 경우 예외 처리
  - [x] RuntimeException 대신 구체적 예외 처리

- [x] JDBC 탬플릿의 중복된 try catch문 제거
