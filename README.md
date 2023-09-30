# JDBC 라이브러리 구현하기

## 1단계 리뷰
- [x] final 일관성 지키기
- [x] update 쿼리 수정
- [x] rowMapper에 대한 고민.. 상수 or 변수 + 컨벤션
- [x] @FunctionalInterface의 장점?
- [x] getFetchSize() 메서드 X

## 2단계 리뷰

### 1차
- [x] findAll의 테스트에서 2개 이상의 값을 통해 테스트하도록 수정
- [x] findByAccount_resultSizeTwo_fail() 테스트 메서드명 수정
  - https://dzone.com/articles/7-popular-unit-test-naming
- [x] JdbcTemplate에서 null을 반환하기보다 Optional을 반환하도록 수정
- [x] JdbcTemplate의 PreparedStatement 생성 책임 분리
- [x] JdbcTemplate try-catch 중복 제거
- [x] queryForObject의 validateSingleRow 개선
- [ ] JdbcTemplate이 TransactionManager를 가지는 이유 고민
- [x] UserHistoryDao도 JdbcTemplate 사용하도록 수정

### 2차
- [x] PreparedStatementCreator 테스트 작성
- [x] 람다 괄호 제거
- [x] queryExecutor 매개변수 final 추가
- [x] queryExecutor 메서드명 수정
- [x] getSingleQueryResult, getMultipleQueryResult 하나의 메서드로 합치고, 검증을 호출부에서 하기
- [x] 변수명 약어 사용 X