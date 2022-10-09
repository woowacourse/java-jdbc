# JDBC 라이브러리 구현하기

## 1단계
- JdbcTemplate 클래스에서 JDBC와 관련된 처리를 담당한다.
  - DB connection 및 prepareStatement 처리를 JdbcTemplate에서 진행한다.

## 2단계

### 자바가 제공하는 기능을 활용해 클린 코드를 작성 연습
- 익명 클래스
- 함수형 인터페이스
- 제네릭
- 가변 인자
- 람다
- try-with-resources
- checked vs unchecked exception

### 제공해야 하는 라이브러리 기능
  - [X] Connection 생성
  - [X] Statement 준비 및 실행
  - [X] ResultSet 생성
  - [X] 예외 처리
  - [X] Connection, Statement, ResultSet 객체 close

## 3단계
- [X] 트랜잭션 경계 설정
- [X] 트랜잭션 동기화하기 
- [ ] 트랜잭션 서비스 추상화하기
  - 비즈니스 로직과 데이터 액세스 로직을 분리
