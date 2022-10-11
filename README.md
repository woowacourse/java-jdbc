<p align="center">
    <img src="./woowacourse.png" alt="우아한테크코스" width="250px">
</p>

# JDBC 라이브러리 구현하기

---

![Generic badge](https://img.shields.io/badge/Level4-JDBC-green.svg)
![Generic badge](https://img.shields.io/badge/test-12_passed-blue.svg)
![Generic badge](https://img.shields.io/badge/version-3.0.0-brightgreen.svg)

> 우아한테크코스 웹 백엔드 4기, JDBC 구현하기 저장소입니다.

## 학습목표

- JDBC 라이브러리를 구현한다.
- 데이터베이스에 대한 이해도를 높인다.

<br>

## 🚀 3단계 - Transaction 적용하기

---

### 미션 설명

- User의 비밀번호를 변경할 수 있는 기능을 추가하자.
- 해당 기능은 UserService 클래스의 changePassword() 메서드에 구현되어있다.
- 비밀번호를 변경하면 누가, 언제, 어떤 비밀번호로 바꿨는지 이력을 남겨야 한다.
- 이력이 있어야 고객센터에서 고객 문의를 대응할 수 있다.
- 고객의 변경 이력을 확인 할 수 있도록 changePassword() 메서드는 비밀번호 변경과 이력을 남기도록 구현되어 있다.

- 하지만 changePassword() 메서드는 원자성(Atomic)이 보장되지 않는다.
- 중간에 예외가 발생해서 작업을 완료할 수 없다면 작업을 원래 상태로 되돌려야 한다.
- 즉, 비밀번호를 바꾸고 이력을 남기는 도중에 에러가 발생하면 원래 비밀번호로 돌려놔야한다.
- 원자성을 보장하기 위해 트랜잭션을 적용하자.

### 체크리스트

- [x] 트랜잭션 롤백이 적용되어 UserServiceTest 클래스의 testTransactionRollback() 테스트 케이스가 통과한다.
- [x] 트랜잭션 서비스와 애플리케이션 서비스가 분리되었다.

### 키워드

- 트랜잭션
- 트랜잭션 동기화
- 트랜잭션 서비스 추상화

### 학습한 내용

- [JdbcTemplate-Study.md](./JdbcTemplate-Study.md)

<br><br>

## 🚀 2단계 - 리팩터링 힌트

---

### 미션 설명

- 자바가 제공하는 기능을 극한으로 활용해 클린 코드를 작성하는 연습을 한다.
- `익명 클래스`, `함수형 인터페이스`, `제네릭`, `가변 인자`, `람다`, `try-with-resources`, `checked vs unchecked exception`

<br>

### 레벨2 미션 개인 목표

- [x] 미션 설명에서 제안한 기능들은 step1에서 이미 적용되었다.
- [x] JDBC 라이브러리와 개발자간 기능 구현 분리에 대해서도 LMS 예시대로 step1에서 적용되었다.
    - JDBC 라이브러리 : Connection 생성, Statement 준비 및 실행, ResultSet 생성, 예외 처리, 트랜잭션 관리, Connection, Statement, ResultSet 객체
      close
    - 개발자 : 연결 매개변수 설정, SQL문 작성, SQL문의 매개변수와 값 설정, ResultSet에서 데이터 추출
- [x] 추상 클래스, 템플릿 메서드 패턴을 적용하기에는 update/insert에 중복 코드가 없으며,도메인 의존도도 존재하지 않는다.
- [x] `JdbcTemplate`의 `command`, `queryForList` 메서드는 템플릿 코드가 중복되고 있다.
    - 이 지점에 대해서 중복 제거를 도전해보자.
    - 템플릿 콜백 패턴을 이용해 `JdbcTemplate`의 중복되던 내용을 템플릿화 하여 `command`와 `queryForList`의 중복을 제거했다.

<br><br>

## 🚀 1단계 - JDBC 라이브러리 구현하기

---

### 미션 설명

- 현재 대시보드 웹서비스는 메모리에 사용자 정보를 저장하고 있다.
  메모리에 저장하다보니 서버를 재시작하면 기록했던 데이터가 모두 날라간다.
  그리고 서버를 2대 이상 사용하면 메모리에 저장한 데이터를 공유할 수 없다.
  웹서비스를 운영하려면 DB가 필요하다.

- 사용자 데이터를 메모리가 아닌 DB에 저장할 수 있도록 개선이 필요하다.

- 자바 진영에서는 애플리케이션의 DB 관련 처리를 위해 JDBC API를 제공한다.

- 문서를 참고해 JDBC API를 적용해보니 반복적인 DB 관련 작업을 수행하는 코드가 나타났다.
  그리고 프레임워크를 사용하는 개발자 입장에서 매번 복잡한 코드를 작성하다보니 생산성이 떨어진다.

<br>

### 기능 요구 사항

> 개발자는 SQL 쿼리 작성, 쿼리에 전달할 인자, SELECT 구문일 경우 조회 결과를 추출하는 것만 집중할 수 있도록 라이브러리를 만들자.

<br>

### 체크리스트

- [x] UserDaoTest의 모든 테스트 케이스가 통과한다.
- [x] UserDao가 아닌 JdbcTemplate 클래스에서 JDBC와 관련된 처리를 담당하고 있다.

<br>

### 1차 피드백

- [x] RowMapper 인터페이스에 rowNum이 없어서 불편하지 않은가요?
    - 2007년 스프링 프로젝트
      이슈로 [비슷한 논의](https://github.com/spring-projects/spring-framework/issues/7796#issuecomment-453314824)가 있었음.
    - 유겐 휠러의 답변에 모두 수긍되나, 현재 필요하지 않으므로 필요할 때까지 구현하지 않기로 함
    - 라이브러리는 사용자의 확장성을 고려해 `too much`가 `too little` 보다 낫다는 관점도 있지만 현재 미션에서 집중할 부분이 아니라고 판단
- [x] 복수의 행을 질의할 때에 RowMapper로 단일 행이 아닌 모든 행에 대한 처리를 직접 구현해야 하는 부분
    - API 사용 방법이 획일화 되어 러닝 커브가 낮아진다는 장점이 있으나 사용 편의성이 저해됨
    - 클라이언트에게 `queryForList`, `queryForOne` API를 구분하여 사용하도록 유도하되, RowMapper는 재사용 가능하도록 구현
- [x] insert와 update요청이 모두 JdbcTemplate#insert 에 의존하는 문제
    - command와 query로 분리
    - CUD는 `command`, R은 `queryForList`, `queryForOne`으로 구현
- [x] JdbcTemplateTest 관련
    - 리뷰어 아서 제공 실제
      구현된 [테스트 코드 링크](https://github.com/spring-projects/spring-framework/blob/main/spring-jdbc/src/test/java/org/springframework/jdbc/core/JdbcTemplateTests.java)
    - 참고하여 테스트 코드 추가
- [x] 라이브러리 사용자가 커서를 옮기는 `rs.next()`를 직접 호출하는 부분은 확실히 개선이 필요하다고 느껴짐
    - 클라이언트는 복수, 단수와 상관 없이 단일 행에 대한 RowMapper만 구현하도록 개선
    - JdbcTemplate 내부에서 커서를 핸들링하도록 개선

<br>

### 힌트

- 리팩터링은 UserDaoTest를 활용해 진행한다.
- 중복을 제거하기 위한 라이브러리는 JdbcTemplate 클래스에 구현한다.
- DataSource는 DataSourceConfig 클래스의 getInstance() 메서드를 호출하면 된다.

<br><br>
