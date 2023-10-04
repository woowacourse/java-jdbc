# JDBC 라이브러리 구현하기

## 1단계 기능 요구 사항

- [x] JDBC 생성자 구현
- [x] insert 메서드 구현
- [x] update 메서드 구현
- [x] findAll 메서드 구현
- [x] findById 메서드 구현
- [x] findByAccount 메서드 구현

## 1,2 단계 피드백 요구 사항

- [ ] PreparedStatementExecutor의 RuntimeException을 커스텀 예외로 처리
- [x] 어노테이션을 통해 함수형 인터페이스 명시
- [ ] JdbcTemplate.class getPreparedStatementCaller 메서드의 ResultSet 결과가 2개 이상인 경우 처리
- [x] 컨벤션 통일
- [ ] Optional을 사용하여 null 처리
- [x] ResultSet의 객체를 가져올 때 columnLabel을 이용해 컬럼 명시 
