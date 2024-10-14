# 만들면서 배우는 스프링

## JDBC 라이브러리 구현하기

### 1단계 요구사항

- [x] 개발자는 SQL 쿼리 작성, 쿼리에 전달할 인자, SELECT 구문일 경우 조회 결과를 추출하는 것만 집중할 수 있도록 라이브러리를 만들기
    - [x] `UserDaoTest`가 통과하도록 만들기
    - [x] jdbcTemplate 클래스로 db 관련 로직 이관하기
        - [x] jdbcTemplate 테스트 코드 작성

### 2단계 요구사항(리팩터링)

- [x] 불필요한 메서드 제거
    - [x] setParameters 메서드 인터페이스로 분리

- [x] 라이브러리 확장
    - [x] PreparedStatementSetter 인터페이스가 가변인자를 사용
    - [x] 람다를 적극적으로 활용해서 코드량을 줄여보자.

- [x] 1단계 피드백 반영
    - [x] `queryForObject`가 `Optional`을 반환하도록 변경
    - [x] `queryForObject` 반환 로직에 예외 추가
    - [x] `JdbcTemplateTest` 변경

### 3단계 요구사항(트랜잭션)

- [x] `changePassword` 메서드의 원자성을 보장하기 위해 트랜잭션 적용
    - [x] `UserServiceTest`가 돌아가도록 로직 수정하기
        - [x] 트랜잭션 경계 설정
        - [x] userDao, userHistoryDao이 동일한 Connection 객체를 사용하도록 변경
- [x] 2단계 피드백 반영
    - [x] `parameter` 개수 검증 로직 추가 후 터지던 `JdbcTemplate` 테스트 수정

### 4단계 요구사항(트랜잭션 동기화)

- [x] 트랜잭션 동기화(Transaction synchronization) 방식 사용
    - [x] 서비스와 DAO에서 DataSourceUtils를 사용해 connection 설정
    - [x] TransactionSynchronizationManager 클래스가 올바르게 작동하도록 구현
- [ ] 트랜잭션 서비스 추상화
    - [ ] 인터페이스를 활용하여 트랜잭션 서비스를 추상화하여 비즈니스 로직과 데이터 액세스 로직을 분리
        - [ ] `UserService` 인터페이스 추가
            - [ ] UserService 인터페이스를 구현한 클래스 2개 생성
                - [ ] `AppUserService`
                - [ ] `TxUserService`
    - [ ] `UserServiceTest`가 통과하도록 만들기
        - [ ] `testTransactionRollback()` 테스트 메서드 변경

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
