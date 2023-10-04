# JDBC 라이브러리 구현하기

## 1단계
- [x] UserDaoTest 성공시키기
  - [x] update()
  - [x] findAll()
  - [x] findByAccount()
- [x] 중복 코드는 JdbcTemplate 에 추출하기

## 2단계
- [ ] 자바 기능 활용하기
  - 익명 클래스, ~~함수형 인터페이스~~, ~~제네릭~~, ~~가변 인자~~, ~~람다~~, ~~try-with-resources~~, checked vs unchecked exception
- [ ] 라이브러리가 하는지 확인
  - [x] Connection 생성
  - [x] Statement 준비 및 실행
  - [ ] ResultSet 생성
  - [ ] 예외 처리
  - [ ] 트랜잭션 관리 -> 3단계
  - [x] Connection, Statement, ResultSet 객체 close
