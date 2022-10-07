# JDBC 라이브러리 구현하기

## 1단계 요구사항

- [x] UserDaoTest의 모든 테스트 케이스가 통과한다.
- [x] UserDao가 아닌 JdbcTemplate 클래스에서 JDBC와 관련된 처리를 담당하고 있다.

## 2단계 요구사항

| 동작                                        | JDBC 라이브러리 | 개발자 |
|-------------------------------------------|------------|-----|
| 연결 매개변수 설정                                | 	          | ✅   |
| Connection 생성                             | ✅          |
| SQL문 작성                                   |            | ✅   |
| SQL문의 매개변수와 값 설정                          |            | ✅   |
| Statement 준비  및 실행                        | ✅          |
| ResultSet 생성                              | ✅          |
| ResultSet에서 데이터 추출                        |            | ✅   |
| 예외 처리                                     | ✅          |
| 트랜잭션 관리                                   | ✅          |
| Connection, Statement, ResultSet 객체 close | ✅          |
