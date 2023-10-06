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

### 3차
- [x] UserDao ResultSet final 붙이기
- [x] SingleResult 정팩메 네이밍 수정
- [x] DataSourceConfig EOF

## 3단계 리뷰
- [x] ThreadLocal을 활용한 트랜잭션 동기화(Connection 공유)
- [x] jdbc 모듈 내에 접근제어자 수정
- [x] 콜백 메서드 예외 처리 수정 
  - 콜백 메서드에서는 SQLException이 발생하지 않는다!

### 2차
- [x] TransactionCallback의 Connection 제거
- [x] 여러 검증에 대해서는 assertAll 또는 SoftAssertions 활용
- [x] TransactionTemplate 테스트 코드 작성
- [x] 공유자원 Singleton vs static
- [x] JdbcTemplate 테스트 작성
- [x] JdbcTemplate의 executeQuery 중복 제거
- [x] TransactionTemplate catch 절 final

### 3차
- [x] EOF 해결(TransactionTemplateTest, PreparedStatementCreatorTest)

## 4단계 리뷰
- [x] AppUserService 생성자 빈칸 제거 
- [ ] TxUserService TransactionTemplate 생성자로 받도록 수정
- [ ] TransactionTemplate 반환값 없는 메서드 만들기
- [ ] Nullable TypeQualifierNickname 어떤 효과?
- [ ] TransactionSyncrhonizationManager 중복되는 get() 제거
- [ ] ConnectionManager 제거 리팩토링
