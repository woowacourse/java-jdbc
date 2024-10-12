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

### 1단계 - JDBC 라이브러리 구현하기

- [x] 개발자는 SQL 쿼리 작성, 쿼리에 전달할 인자, SELECT 구문일 경우 조회 결과를 추출하는 것만 집중할 수 있도록 라이브러리를 만들자.
- [x] 리팩터링은 UserDaoTest를 활용해 진행
- [x] 중복을 제거하기 위한 라이브러리는 JdbcTemplate 클래스에 구현
- [x] DataSource는 DataSourceConfig 클래스의 getInstance() 메서드를 호출

### 2단계 - 리팩토링

- [x] 자바가 제공하는 기능을 극한으로 활용해 클린 코드를 작성하는 연습을 한다.
    - [x] 메서드 추출
    - [x] 클래스 추출
    - [x] 템플릿 메서드 패턴 적용
    - [x] 도메인 의존도 제거
    - [x] 다시 한 번 클래스 추출
    - [x] 다시 한 번 템플릿 메서드 적용
    - [x] 불필요한 mapRow 메서드 제거
    - [x] 라이브러리 확장

### 3단계 - Transaction 적용하기

- [ ] 비밀번호를 바꾸고 이력을 남기는 도중에 에러가 발생하면 원래 비밀번호로 돌려놓기 (원자성 보장)
- [x] UserServiceTest 클래스에서 @Disabled를 삭제하고 미션을 진행
- [ ] userDao와 userHistoryDao를 한 트랜잭션으로 묶으려면 동일한 Connection 객체를 사용하도록 변경
