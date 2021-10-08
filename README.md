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
- [x] Controller 에서 Dao 사용하는 방향으로 리팩터링

## 리팩터링 
- [x] JdbcTemplate 리팩터링
  - ResultSet 생성 및 추출 전략 콜백과 전략을 사용하는 ResultSetRunner 생성
-[x] JdbcTemplate 테스트
  - [x] ResultSetRunner 테스트
- [x] core 모듈 생성
- [ ] DI 구현
  - [x] ComponentScanner 구현
    - @Component 가 붙어있는 어노테이션들을 찾는다. (Controller, Service, Repository etc..)
    - 각각 어노테이션이 붙어있는 클래스를 찾는다.
  - [x] ComponentContainer 구현
  - [x] FieldInjectionStrategy 구현
  - [x] ConstructorInjectionStrategy 구현
  - [x] InjectStrategyRegistry 구현 
  - [x] DependencyGraph 구현
  - [x] MethodInjectStrategy 구현
  
