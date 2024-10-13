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

# JDBC 라이브러리 구현하기

## 1단계 - JDBC 라이브러리 구현하기

- [x] 개발자가 SQL 쿼리 작성, 결과를 추출하는 것에만 집중할 수 있도록 라이브러리를 구현한다.
    - [x] 커넥션을 조회하고 반환하는 로직을 라이브러리가 제공하도록 구현한다.
    - [x] PreparedStatement 생성 및 파라미터 바인딩을 라이브러리가 제공하도록 구현한다.
    - [x] 예외 발생시 추상화된 예외로 변환하여 라이브러리 종속성을 개선한다.
    - [x] 리소스를 안전하게 종료시킨다.

## 2단계 - 리팩토링

자바가 제공하는 기능을 극한으로 활용해 클린 코드를 작성하는 연습을 한다.

- [x] 함수형 인터페이스를 활용하여 중복 try-catch를 제거한다.
    - [x] PreparedStatementCallback 함수형 인터페이스를 활용한다.
- [x] 함수형 인터페이스를 활용하여 사용자가 직접 파라미터 설정을 할 수 있도록 메서드를 제공한다.
    - [x] PreparedStatementSetter 함수형 인터페이스를 활용한다.

## 3단계 - Transaction 적용하기

- [x] 동일한 트랜잭션을 사용할 수 있도록 Dao 메서드가 같은 Connection을 공유한다.
