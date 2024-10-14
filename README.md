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
    - JDBC 라이브러리: ~~Connection 생성~~, ~~Statement 준비 및 실행~~, ~~ResultSet 생성~~, ~~예외 처리~~, 트랜잭션 관리, ~~
      Connection*Statement*ResultSet 객체 Close~~
    - 개발자: ~~연결 매개변수 설정~~, ~~SQL문 작성~~, ~~SQL문의 매개변수와 값 설정~~, ResultSet에서 데이터 추출

### 3단계 - Transaction 적용하기

- [x] UserServiceTest에서 @Disabled 삭제
- [x] changePassword() 메서드에 트랜잭션 적용해 원자성 보장
    - [x] Connection 객체의 SetAutoCommit(false) 메서드 호출해 트랜잭션 시작
    - [x] 비즈니스 로직 끝나면 트랜잭션 커밋 또는 롤백 실행
    - [x] userDao와 userHistoryDao를 한 트랜잭션으로 묶기 위해 동일한 Connection 객체 사용하도록 변경

### 4단계 - Transaction synchronization 적용하기

- [ ] 트랜잭션 동기화 적용(Connection 객체를 따로 보관해두고, DAO에서 호출할 때 저장된 Connection을 가져다 사용하는 방식)
    - [ ] 서비스와 DAO에서 Connection 객체를 가져오는 부분은 DataSourceUtils를 사용
    - [ ] TransactionSynchronizationManager 클래스가 올바르게 작동하도록 구현
- [ ] 트랜잭션 서비스 추상화(인터페이스를 활용하여 트랜잭션 서비스를 추상화하여 비즈니스 로직과 데이터 액세스 로직을 분리)
    - [ ] UserService 인터페이스 추가
    - [ ] UserService 인터페이스를 구현한 클래스 2개 추가 (AppUserService, TxUserService)
    - [ ] UserServiceTest 클래스의 testTransactionRollback() 테스트 메서드 수정 후 통과

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
