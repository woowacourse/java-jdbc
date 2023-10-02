# JDBC 라이브러리 구현하기

개발자는 SQL 쿼리 작성, 쿼리에 전달할 인자, SELECT 구문일 경우
조회 결과를 추출하는 것만 집중할 수 있도록 라이브러리를 만들자.

## 힌트

- 리팩터링은 UserDaoTest를 활용해 진행한다.
- 중복을 제거하기 위한 라이브러리는 JdbcTemplate 클래스에 구현한다.
- DataSource는 DataSourceConfig 클래스의 getInstance() 메서드를 호출하면 된다.

## 2단계

자바가 제공하는 기능을 극한으로 활용해 클린 코드를 작성하는 연습을 한다.

- 익명 클래스
- 함수형 인터페이스
- 제네릭
- 가변 인자
- 람다
- try-with-resources
- checked vs unchecked exception

### 어떤 동작을 라이브러리로 만들까?

| 동작                                        | JDBC 라이브러리 | 개발자 |
|:------------------------------------------|:----------:|:---:|
| 연결 매개변수 설정                                |            |  ✅  |
| Connection 생성                             |     ✅      |     |
| SQL문 작성                                   |            |  ✅  |
| SQL문의 매개변수와 값 설정                          |            |  ✅  |
| Statement 준비 및 실행                         |     ✅      |     |
| ResultSet 생성                              |     ✅      |     |
| ResultSet에서 데이터 추출                        |            |  ✅  |
| 예외 처리                                     |     ✅      |     |
| 트랜잭션 관리                                   |     ✅      |     |
| Connection, Statement, ResultSet 객체 close |     ✅      |     |

