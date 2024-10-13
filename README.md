# 만들면서 배우는 스프링

## JDBC 라이브러리 구현하기

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

## 1단계 - JDBC 라이브러리 구현하기

- [x] UserDao 미구현 메서드 구현
- [x] JdbcTemplate 구현

## 2단계 - 리팩터링

- [x] PreparedStatementSetter 분리
- [x] 메서드 예외 처리 중복 제거

## 3단계 - Transaction 적용하기

- [x] 트랜잭션 경계 설정하기
  - [x] UserDao, UserHistoryDao 커넥션 객체 사용하도록 수정
  - [x] UserService 에서 한 커넥션으로 관리하도록 수정

## 4단계 - Transaction synchronization 적용하기

- [ ] Transaction synchronization 적용
- [ ] 트랜잭션 서비스 추상화
