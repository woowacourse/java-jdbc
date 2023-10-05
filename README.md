# JDBC 라이브러리 구현하기

## 1단계

- [x] UserDaoTest 성공시키기
    - [x] update()
    - [x] findAll()
    - [x] findByAccount()
- [x] 중복 코드는 JdbcTemplate 에 추출하기

## 2단계

- [x] 자바 기능 활용하기
    - ~~익명 클래스~~, ~~함수형 인터페이스~~, ~~제네릭~~, ~~가변 인자~~, ~~람다~~, ~~try-with-resources~~, ~~checked vs unchecked exception~~
- [ ] 라이브러리가 하는지 확인
    - [x] Connection 생성
    - [x] Statement 준비 및 실행
    - [x] ResultSet 생성
    - [x] 예외 처리
    - [ ] 트랜잭션 관리
    - [x] Connection, Statement, ResultSet 객체 close

## 3단계
- [ ] changePassword() 메서드에 원자성을 보장하기 위해 트랜잭션을 적용
- [ ] userDao와 userHistoryDao를 한 트랜잭션으로 묶으려면 동일한 Connection 객체를 사용하도록 변경
