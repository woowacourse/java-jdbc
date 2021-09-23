# jwp-dashboard-jdbc

## 🚀 1단계 - JDBC 라이브러리 구현하기

- [x] DataSourcePopulatorUtils jdbc 모듈로 이동 + 리팩터링
- [x] JdbcTemplate 생성 + UserDao 의 insert() 리팩터링
- [x] DataSourceBuilder 생성
- [x] UserDao JdbcTemplate 사용하는 방향으로 구현
    - [x] insert()
    - [x] findById()
    - [x] findByAccount()
    - [x] findAll()
    - [x] update
    - [x] delete
- [x] ResultSetExtractorTest 테스트
- [ ] Controller 에서 Dao 사용하는 방향으로 리팩터링

## 리팩터링 예정
- [ ] JdbcTemplate 테스트
- [ ] JdbcTemplate 분리할만한 부분 찾기
- [ ] @Inject 사용
