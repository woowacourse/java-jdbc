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

## 🚀 3단계 - Transaction 적용하기

- [ ] User의 비밀번호를 변경할 수 있는 기능을 추가한다.
    - 해당 기능은 UserService 클래스의 changePassword() 메서드에 구현되어있다.
    - 비밀번호를 변경하면 누가, 언제, 어떤 비밀번호로 바꿨는지 이력을 남겨야 한다.
    - changePassword() 메서드는 비밀번호 변경과 이력을 남기도록 구현되어 있다.

    - 하지만 changePassword() 메서드는 원자성(Atomic)이 보장되지 않는다.
    - 중간에 예외가 발생해서 작업을 완료할 수 없다면 작업을 원래 상태로 되돌려야 한다.
    - 즉, 비밀번호를 바꾸고 이력을 남기는 도중에 에러가 발생하면 원래 비밀번호로 돌려놔야 한다.
    - 원자성을 보장하기 위해 트랜잭션을 적용해야 한다.

    - 현재 userDao와 userHistoryDao는 각각 Connection 객체를 만들기 때문에 개별적으로 트랜잭션이 생성된다.
    - userDao와 userHistoryDao를 한 트랜잭션으로 묶으려면 동일한 Connection 객체를 사용하도록 변경해야 한다.
