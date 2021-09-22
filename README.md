# jwp-dashboard-jdbc

## 1단계 기능 요구사항
- [x] JDBC 라이브러리 구현하기

## 2단계 기능 요구사항
- [ ] 리팩터링 힌트
  - [x] 메서드 추출
  - [ ] UpdateJdbcTemplate, InsertJdbcTemplate 클래스 추출
  - [ ] 템플릿 메서드 패턴 적용
  - [ ] 도메인 의존도 제거
  - [ ] SelectJdbcTemplate 클래스 추출
  - [ ] mapRow, setValues 인터페이스로 분리
  - [ ] Unchecked Exception 추가
  - [ ] RowMapper 제네릭 사용하도록 수정
  - [ ] PreparedStatementSetter 가변인자를 사용하도록 수정
