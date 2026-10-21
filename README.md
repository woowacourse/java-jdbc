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


------

[공부한 내용]

1. RowMapper 인터페이스로 조회 결과 매핑 분리
JdbcTemplate에서 User 객체를 직접 생성하는 대신, RowMapper로 전달받은 필드 매핑 방법을 적용해 객체를 반환하도록 했다.
처음에는 JdbcTemplate에서 조회 결과를 직접 User 객체로 만들어 반환하려고 했다. 
하지만, 이렇게 하면 JdbcTemplate이 User에 의존하게 된다. 
이를 해결하기 위해 UserDao에서 RowMapper로 매핑 방법을 정의하고 JdbcTemplate에 전달했다. 
매핑 자체를 반환하는 것이 아니라, 전달받은 매핑 방법으로 객체를 만들어 반환하는 구조가 되었다.


2. UserDao에서 DataSource 대신 JdbcTemplate을 필드로
각 메서드에서 DataSource로 JdbcTemplate을 생성하던 중복 코드를 제거하고, JdbcTemplate을 필드에 저장해 재사용하도록 했다. 
이로써, UserDao에는 SQL과 인자, 결과 매핑 코드만 남고, DB 연결과 실행, 예외 처리, 자원 정리는 JdbcTemplate에 맡맡기게 되었다.
DB 연결과 자원 관리를 JdbcTemplate에 맡기면서 UserDao가 DataSource를 직접 사용할 필요도 없어졌다.