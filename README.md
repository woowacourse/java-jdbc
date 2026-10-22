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

## 미션 진행

<details>
<summary>Step 1 - JDBC 라이브러리 구현하기</summary>

- [x] UserDao에 사용자 저장, 조회, 수정 기능을 구현한다.
- [x] UserDaoTest를 통과시켜 리팩터링 전의 기본 동작을 확인한다.
- [x] JdbcTemplate에 SQL과 인자를 받아 저장, 수정 SQL을 실행하는 공통 기능을 구현한다.
- [x] UserDao의 insert(), update()가 JdbcTemplate의 공통 기능을 사용하도록 변경한다.
- [x] 저장, 수정 공통화 후 UserDaoTest 5개가 통과하는지 확인한다.
- [x] JdbcTemplate에 SQL, 인자, 결과를 객체로 변환하는 방법을 받아 조회하는 공통 기능을 구현한다.
- [x] UserDao의 findById(), findByAccount(), findAll()이 JdbcTemplate의 공통 기능을 사용하도록 변경한다.
- [x] JdbcTemplate에서 사용한 Connection, PreparedStatement, ResultSet을 정리한다.
- [x] UserDaoTest를 다시 통과시켜 리팩터링 후에도 기존 동작이 유지되는지 확인한다.
- [x] 여러 건 조회, 빈 결과, JdbcTemplate 주입 생성자를 검증한다.
- [x] SQL 실행과 결과 매핑 실패 시 원인 예외 유지와 자원 정리를 검증한다.

#### 구현 내용

- `update(sql, params)`가 저장, 수정 등 변경 SQL을 실행한다.
- `query(sql, params, rowMapper)`가 각 행을 변환해 목록으로 반환한다.
- `queryForObject(sql, params, rowMapper)`는 첫 번째 결과를 반환하며 결과가 없으면 `null`을 반환한다.
- UserDao는 SQL, 인자, ResultSet에서 User로 변환하는 방법만 담당한다.
- JdbcTemplate은 연결, 인자 설정, SQL 실행, 결과 순회, 예외 처리, 자원 정리를 담당한다.
- 현재는 `Object[]`와 Object를 반환하는 RowMapper를 사용한다. 제네릭, 가변 인자, 람다 등의 개선은 2단계에서 진행한다.

#### 최종 검증

```sh
./gradlew :jdbc:test :app:test --rerun-tasks --console=plain
```

- UserDaoTest 9개, JdbcTemplateTest 7개 통과.
- UserDaoTest는 각 테스트 전에 사용자 테이블과 ID를 초기화하여 실행 순서에 의존하지 않는다.
- 3단계 대상인 UserServiceTest 2개는 기존 `@Disabled` 상태를 유지한다.
- study 모듈의 커넥션 풀, 트랜잭션, AOP 실습은 이번 검증과 제출 범위에서 제외한다.

</details>

<details>
<summary>Step 2</summary>

- [ ] RowMapper에 제네릭을 적용해 결과 캐스팅을 제거한다.
- [ ] 가변 인자로 SQL 인자 전달을 편리하게 만든다.
- [ ] 람다와 try-with-resources를 활용해 코드를 정리한다.
- [ ] SQLException을 커스텀 Unchecked Exception으로 변환한다.

</details>

<details>
<summary>Step 3</summary>

- [ ] UserServiceTest의 @Disabled를 제거한다.
- [ ] 비밀번호 변경과 이력 저장에 동일한 Connection을 전달한다.
- [ ] 성공 시 커밋하고 실패 시 롤백하여 원자성을 보장한다.

</details>

<details>
<summary>Step 4</summary>

- [ ] DataSourceUtils와 TransactionSynchronizationManager로 Connection을 공유한다.
- [ ] DAO에 Connection을 파라미터로 전달하지 않도록 변경한다.
- [ ] AppUserService와 TxUserService로 비즈니스 로직과 트랜잭션 처리를 분리한다.

</details>
