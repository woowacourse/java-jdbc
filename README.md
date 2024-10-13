# 만들면서 배우는 스프링

## JDBC 라이브러리 구현하기 기능 요구사항

### 1단계 - JDBC 라이브러리 구현하기

- [x] UserDao 의 미구현된 메서드들 완성
    - [x] findAll, findByAccount, update 구현
- [x] JdbcTemplate 구현해 UserDao 의 중복되는 코드 제거
    - [x] SQL 쿼리에 인자 넣는 작업을 JdbcTemplate이 하도록 수정
    - [x] SQL 쿼리를 실행하는 작업을 JdbcTemplate이 하도록 수정
    - [x] SQL 쿼리 실행 결과를 객체로 매핑하는 작업을 JdbcTemplate이 하도록 수정
- [x] JdbcTemplate 이용해 UserHistoryDao 의 중복되는 코드 제거

### 2단계 - 리팩터링

- [x] 자바가 제공하는 기능 극한으로 활용해 클린 코드 작성
  - ~~함수형 인터페이스~~, ~~제네릭~~, ~~가변인자~~, ~~람다~~, ~~try-with-resources~~, ~~checked vs unchecked exception~~
- [x] JDBC 라이브러리로 만들 동작 명확히 구분
  - JDBC 라이브러리: ~~Connection 생성~~, ~~Statement 준비 및 실행~~, ~~ResultSet 생성~~, ~~예외 처리~~, 트랜잭션 관리, ~~Connection*Statement*ResultSet 객체 Close~~
  - 개발자: ~~연결 매개변수 설정~~, ~~SQL문 작성~~, ~~SQL문의 매개변수와 값 설정~~, ResultSet에서 데이터 추출

### 3단계 - Transaction 적용하기

- [ ] UserServiceTest에서 @Disabled 삭제
- [ ] changePassword() 메서드에 트랜잭션 적용해 원자성 보장
  - [ ] Connection 객체의 SetAutoCommit(false) 메서드 호출해 트랜잭션 시작
  - [ ] 비즈니스 로직 끝나면 트랜잭션 커밋 또는 롤백 실행
  - [ ] userDao와 userHistoryDao를 한 트랜잭션으로 묶기 위해 동일한 Connection 객체 사용하도록 변경

## JDBC 라이브러리 구현하기 가이드

### 학습목표

- JDBC 라이브러리를 구현하는 경험을 함으로써 중복을 제거하는 연습을 한다.
- Transaction 적용을 위해 알아야할 개념을 이해한다.

### 시작 가이드

1. 이전 미션에서 진행한 코드를 사용하고 싶다면, 마이그레이션 작업을 진행합니다.
    - 학습 테스트는 강의 시간에 풀어봅시다.
2. LMS의 1단계 미션부터 진행합니다.

## 준비 사항

- 강의 시작 전에 docker를 설치해주세요.

## 학습 테스트

1. [ConnectionPool](study/src/test/java/connectionpool)
2. [Transaction](study/src/test/java/transaction)
