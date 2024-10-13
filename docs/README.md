## 1단계 미션 요구사항

- [x] 개발자는 SQL 쿼리 작성, 쿼리에 전달할 인자, SELECT 구문일 경우 조회 결과를 추출하는 것만 집중할 수 있도록 라이브러리를 만들자.

## 2단계 미션 요구사항

- [x] 커스텀 Exception 생성을 통한 SqlException 변환
- [x] PreparedStatementSetter 에서 가변인자를 사용하도록 구현
- [x] RowMapper 에서 제네릭을 사용하도록 재구성
- [x] JdbcTemplate 에서 PreparedStatemetSetter를 사용하도록 리팩토링

## 3단계 미션 요구사항

- [x] UserServiceTest 클래스에서 @Disabled를 삭제하고 미션을 진행한다.
- [x] userDao와 userHistoryDao를 한 트랜잭션으로 묶으려면 동일한 Connection 객체를 사용하도록 변경하자.
