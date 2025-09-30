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

## 🚀 1단계 - JDBC 라이브러리 구현하기

### 기능 요구 사항

- [x] 개발자는 SQL 쿼리 작성, 쿼리에 전달할 인자, SELECT 구문일 경우 조회 결과를 추출하는 것만 집중할 수 있도록 라이브러리를 만들어야 한다.

> 리팩터링은 UserDaoTest를 활용해 진행한다.
>
> 중복을 제거하기 위한 라이브러리는 JdbcTemplate 클래스에 구현한다.
>
> DataSource는 DataSourceConfig 클래스의 getInstance() 메서드를 호출하면 된다.

## 🚀 2단계 - 리팩터링

- 미션 설명) 자바가 제공하는 기능을 극한으로 활용해 클린 코드를 작성하는 연습을 한다.
    - 익명 클래스 / 함수형 인터페이스 / 제네릭 / 가변 인자 / 람다 / try-with-resources / checked vs unchecked exception

### 기능 요구 사항

- [x] 아래의 동작들에 맞춰 코드를 리팩터링 한다.
    - 개발자의 동작:
        - 연결 매개변수 설정
        - SQL문 작성
        - SQL문의 매개변수와 값 설정
        - Result Set에서 데이터 추출
    - JDBC 라이브러리의 동작:
        - Connection 생성
        - Statement 준비 및 실행
        - ResultSet 생성
        - 예외처리
        - 트랜젝션 관리
        - Connection, Statement, ResultSet 객체 close
