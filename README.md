# JDBC 라이브러리 구현하기

## 1단계 요구사항

- [x] 개발자가 SQL 쿼리 작성, 쿼리에 전달할 인자, SELECT 구문일 경우 조회 결과를 추출하는 것만 집중할 수 있도록 라이브러리 생성
- 리팩터링
  - [x] JdbcTemplate 내 리소스 관리 try-with-resources 구문으로 변경
  - [x] JdbcTemplate 내 중복 코드 제거
  - [x] Class 타입으로 바로 받을 수 있도록 JdbcTemplate 메서드 추가
