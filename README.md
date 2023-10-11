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
- [x] 라이브러리가 하는지 확인
    - [x] Connection 생성
    - [x] Statement 준비 및 실행
    - [x] ResultSet 생성
    - [x] 예외 처리
    - [x] 트랜잭션 관리
    - [x] Connection, Statement, ResultSet 객체 close

## 3단계
- [x] changePassword() 메서드에 원자성을 보장하기 위해 트랜잭션을 적용
- [x] userDao와 userHistoryDao를 한 트랜잭션으로 묶으려면 동일한 Connection 객체를 사용하도록 변경

## 4단계
- [x] 트랜잭션 동기화(Transaction synchronization) 적용
  - [x] 트랜잭션을 시작하기 위한 Connection 객체를 따로 보관해두고, DAO에서 호출할 때 저장된 Connection을 가져다 사용
  - [x] 서비스와 DAO에서 Connection 객체를 가져오는 부분은 DataSourceUtils를 사용하도록 수정
  - [x] TransactionSynchronizationManager 클래스가 올바르게 작동하도록 구현
- [x] 트랜잭션 서비스 추상화
  - [x] 인터페이스를 활용하여 트랜잭션 서비스를 추상화하여 비즈니스 로직과 데이터 액세스 로직을 분리
