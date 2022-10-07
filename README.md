# JDBC 라이브러리 구현하기

## 기능 요구 사항

개발자는 SQL 쿼리 작성, 쿼리에 전달할 인자, SELECT 구문일 경우 조회 결과를 추출하는 것만 집중할 수 있도록 라이브러리를 만들자.

## 체크리스트

- [x] UserDaoTest의 모든 테스트 케이스가 통과한다.
- [x] UserDao가 아닌 JdbcTemplate 클래스에서 JDBC와 관련된 처리를 담당하고 있다.

## 기능 구현 목록
### JdbcTemplate
- update
- queryForObject
- query

## 🚀 2단계 - 리팩터링

- [x] Connection 생성
- [x] Statement 준비 및 실행
- [x] ResultSet 생성
- [x] 예외 처리
- [ ] 트랜잭션 관리
- [x] Connection, Statement, ResultSet 객체 close
