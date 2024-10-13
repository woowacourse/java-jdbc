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

---

## 1단계 - JDBC 라이브러리 구현하기

- [x] JDBC 라이브러리 구현
  - 개발자는 SQL 쿼리 작성, 쿼리에 전달할 인자, SELECT 구문일 경우 조회 결과를 추출하는 것만 집중

## 2단계 - 리팩터링

- [x] 커스텀 Exception 추가해서 사용자는 Unchecked Exception 되도록 변경
- [x] 제네릭 사용하도록 RowMapper 인터페이스 개선
- [x] 가변인자를 사용해 PreparedStatementSetter 인터페이스 개선
- [x] 람다로 코드량 줄이기

## 3단계 - Transaction 적용하기

- [x] 트랜잭션을 적용해 `changePassword()` 메서드의 원자성 보장

## 4단계 - Transaction synchronization 적용하기

- [x] Transaction synchronization 적용
- [x] 인터페이스로 트랜잭션 서비스를 추상화하여 비즈니스 로직과 데이터 액세스 로직을 분리
