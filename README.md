# JDBC 라이브러리 구현하기

## 1단계 리뷰
- [x] final 일관성 지키기
- [x] update 쿼리 수정
- [x] rowMapper에 대한 고민.. 상수 or 변수 + 컨벤션
- [x] @FunctionalInterface의 장점?
- [x] getFetchSize() 메서드 X

## 2단계 리뷰
- [x] findAll의 테스트에서 2개 이상의 값을 통해 테스트하도록 수정
- [x] findByAccount_resultSizeTwo_fail() 테스트 메서드명 수정
  - https://dzone.com/articles/7-popular-unit-test-naming
- [x] JdbcTemplate에서 null을 반환하기보다 Optional을 반환하도록 수정
- [x] JdbcTemplate의 PreparedStatement 생성 책임 분리
- [x] JdbcTemplate try-catch 중복 제거
- [x] queryForObject의 validateSingleRow 개선
- [ ] JdbcTemplate이 TransactionManager를 가지는 이유 고민