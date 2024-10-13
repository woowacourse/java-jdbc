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

### 구현할 기능 목록

### 1 & 2 단계

- [x] JDBC 라이브러리 구현하기
    - [x] SQL 쿼리 작성, 쿼리에 전달할 인자, SELECT 구문일 경우 조회 결과를 추출하는 것만 집중할 수 있도록 라이브러리를 만든다.
    - [x] UserDao CRUD 기능 추가
    - [x] 중복을 제거하기 위한 라이브러리는 JdbcTemplate 클래스에 구현한다.

### 3단계

- [x] User의 비밀번호를 변경할 수 있다.
    - [x] UserService 클래스의 changePassword() 메서드에 구현한다.
- [x] 비밀번호를 변경하면 누가, 언제, 어떤 비밀번호로 바꿨는지 이력을 남겨야 한다.
    - [x] 비밀번호를 바꾸고 이력을 남기는 도중에 에러가 발생하면 원래 비밀번호로 돌려놔야한다.
- [x] 트랜잭션 경계를 설정한다.
    - [x] 비즈니스 로직이 끝나면 반드시 트랜잭션 커밋 또는 롤백을 실행한다.
    - [x] userDao와 userHistoryDao를 동일한 Connection 객체를 사용해 한 트랜잭션으로 묶는다.

### 4단계

- [x] Transaction synchronization 적용한다.
    - [x] 트랜잭션을 시작하기 위한 Connection 객체를 따로 보관해두고, DAO에서 호출할 때 저장된 Connection을 가져다 사용한다.
    - [x] 서비스와 DAO에서 Connection 객체를 가져오는 부분은 DataSourceUtils를 사용한다.
- [ ] 트랜잭션 서비스 추상화하기
    - [ ] 인터페이스를 활용하여 트랜잭션 서비스를 추상화하여 비즈니스 로직과 데이터 액세스 로직을 분리한다.  
