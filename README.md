# 1단계 - JDBC 라이브러리 구현하기

- 반복적인 DB 관련 작업을 수행하는 코드의 중복 제거

- 개발자는 SQL 쿼리 작성, 쿼리에 전달할 인자, SELECT 구문일 경우 조회 결과를 추출하는 것만 집중할 수 있도록 라이브러리를 구현하기

## 구현할 기능 목록

- [x] UserDao 구현
  - [x] update 메서드 구현
  - [x] findAll 메서드 구현
  - [x] findByAccount 메서드 구현

- [x] UserDao에서 JdbcTemplate을 이용하도록 리팩터링

# 2단계 - 리팩터링

- [x] 메서드 추출
- [x] 클래스 추출
- [x] 템플릿 메서드 패턴 적용
- [x] User 도메인 의존도 제거
- [x] SelectJdbcTemplate 클래스 추출
- [x] SelectJdbcTemplate을 JdbcTemplate으로 통합
- [x] mapRow setValues 메서드 인터페이스로 분리 

## 라이브러리 확장

- [x] 커스텀 Exception을 추가해서 Checked Exception을는 Unchecked Exception으로 전환
- [x] RowMapper 인터페이스가 제네릭을 사용하도록 개선
- [x] PreparedStatementSetter 인터페이스를 매번 구현하지 않고 가변인자를 사용
- [x] 람다를 활용해서 코드량 줄이기










