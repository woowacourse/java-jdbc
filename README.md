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

### 미션 설명

현재 대시보드 웹서비스는 메모리에 사용자 정보를 저장하고 있다.
메모리에 저장하다보니 서버를 재시작하면 기록했던 데이터가 모두 날라간다.
그리고 서버를 2대 이상 사용하면 메모리에 저장한 데이터를 공유할 수 없다.
웹서비스를 운영하려면 DB가 필요하다.

사용자 데이터를 메모리가 아닌 DB에 저장할 수 있도록 개선이 필요하다.

자바 진영에서는 애플리케이션의 DB 관련 처리를 위해 JDBC API를 제공한다.

문서를 참고해 JDBC API를 적용해보니 반복적인 DB 관련 작업을 수행하는 코드가 나타났다.
그리고 프레임워크를 사용하는 개발자 입장에서 매번 복잡한 코드를 작성하다보니 생산성이 떨어진다.

### 기능 요구 사항

개발자는 SQL 쿼리 작성, 쿼리에 전달할 인자, SELECT 구문일 경우 조회 결과를 추출하는 것만 집중할 수 있도록 라이브러리를 만들자.

#### 해석

`UserDao`를 살펴보니 반복되는 코드가 많다. 분석한 결과 공통으로 필요한 것은 총 3개이다.

1. 실행 할 SQL 구문
2. Connection 객체
3. PreparedStatement 객체

update, insert와 같이 데이터베이스에 반영하는 메서드는 `executeUpdate`를 사용하고,
select 구문처럼 조회하는 메서드는 `executeQuery`를 사용한다.

반복적으로 사용하는 코드는 JdbcTemplate 클래스에 구현하는 것이다.

DataSourceConfig는 이미 만들어져있어서 신경쓰지 않아도 될 것 같다.

#### 해결 과정

- [x] UserDaoTest를 전부 통과 시킨다
- [ ] UesrDao에 있는 중복 메서드를 JdbcTemplate에 옮기고 테스트를 전부 통과 시킨다.
  - [ ] insert, findAll, update, findById, findByAccount를 실행할 때 사용하는 JdbcTemplate메서드가 무엇인지 조사한다.
  - [ ] 레벨 2 미션 살펴본다.