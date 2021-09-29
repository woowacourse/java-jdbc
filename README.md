# jwp-dashboard-jdbc

## step1
- [x] 학습테스트 작성
- [x] UserDao 구현하기
  - [x] InMemoryUserRepository -> UserDao로 대체하기
  - [x] UserDao - update, findAll, findByAccount 구현
- [x] JDBC 라이브러리 구현하기
  - [x] 리팩터링은 UserDaoTest를 활용해 진행한다.
  - [x] 중복을 제거하기 위한 라이브러리는 JdbcTemplate 클래스에 구현한다.
  - [x] sql,에러 로깅하기

## step2
- [ ] 리팩터링

## 이슈
- 로그인시 실패
  - 소스코드와 바이트코드 다름. 빌드 문제?
  - 빌드파일 삭제, 클린/빌드해도 해결안됨
- UserDaoTest findByAccount() 실패함 - 쿼리 결과가 5개로 나옴