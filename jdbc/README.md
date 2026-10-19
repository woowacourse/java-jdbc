# JDBC 라이브러리 구현하기

## 1단계 - JDBC 라이브러리 구현하기

### 기능 요구 사항

현재 대시보드 웹서비스는 메모리에 사용자 정보를 저장하고 있다.
메모리에 저장하다보니 서버를 재시작하면 기록했던 데이터가 모두 날라간다.
그리고 서버를 2대 이상 사용하면 메모리에 저장한 데이터를 공유할 수 없다.

웹서비스를 운영하려면 DB가 필요하다.
사용자 데이터를 메모리가 아닌 DB에 저장할 수 있도록 개선이 필요하다.

자바 진영에서는 애플리케이션의 DB 관련 처리를 위해 JDBC API를 제공한다.
문서를 참고해 JDBC API를 적용해보니 반복적인 DB 관련 작업을 수행하는 코드가 나타났다.

그리고 프레임워크를 사용하는 개발자 입장에서 매번 복잡한 코드를 작성하다보니 생산성이 떨어진다.

- [x] 개발자는 SQL 쿼리 작성, 쿼리에 전달할 인자, SELECT 구문일 경우 조회 결과를 추출하는 것만 집중할 수 있도록 라이브러리를 만들자.
    - [x] 리팩터링은 UserDaoTest를 활용해 진행한다.
    - [x] 중복을 제거하기 위한 라이브러리는 JdbcTemplate 클래스에 구현한다.
    - [x] DataSource는 DataSourceConfig 클래스의 getInstance() 메서드를 호출하면 된다.

## 2단계 - 리팩터링

자바가 제공하는 기능을 극한으로 활용해 클린 코드를 작성하는 연습을 한다.

- 익명 클래스
- 함수형 인터페이스
- 제네릭
- 가변 인자
- 람다
- try-with-resources
- checked vs unchecked exception

어떤 동작을 라이브러리로 만들까?

| 동작                                        | JDBC 라이브러리 | 개발자 |
|-------------------------------------------|------------|-----| 
| 연결 매개변수 설정                                |            | ✅   | 
| Connection 생성	                            | ✅          |     |
| SQL문 작성		                                 |            | ✅   |
| SQL문의 매개변수와 값 설정	                         | 	          | ✅   |
| Statement 준비 및 실행                         | 	✅         |     |
| ResultSet 생성                              | 	✅         |     |
| ResultSet에서 데이터 추출                        | 		         | ✅   |     
| 예외 처리                                     | 	✅         |     |
| 트랜잭션 관리                                   | 	✅         |     |
| Connection, Statement, ResultSet 객체 close | 	✅         |     |

- [ ] 메서드 추출
    - UserDao 클래스에서 insert 메서드를 참고해서 update 메서드를 구현한다.
    - update와 insert 메서드에서 사용자가 수정해야 되는 부분을 메서드로 분리한다.
- [ ] 클래스 추출
    - 메서드를 분리하고 나면 update, insert 메서드에 중복된 코드가 보인다.
    - 각각 새로운 클래스로 분리해보자.
    - UserDao에서 update 메서드와 관련된 코드를 UpdateJdbcTemplate 클래스를 만들어 이동시킨다.
    - insert도 마찬가지로 InsertJdbcTemplate 클래스를 만들어 이동한다.
    - UserDao에서 update, insert 각 메서드는 코드를 이동한 클래스를 사용하도록 수정한다.
    - UpdateJdbcTemplate, InsertJdbcTemplate로 옮긴 메서드는 User, UserDao 객체를 의존하고 있다.
- [ ] 템플릿 메서드 패턴 적용
    - UpdateJdbcTemplate, InsertJdbcTemplate 클래스도 중복된 코드가 보인다. 클래스를 하나만 남기고 제거한다.
    - 남은 클래스를 추상 클래스로 만든다. 사용자가 구현해야되는 부분은 추상 메서드로 만들어보자.
    - 클래스명, 메서드명을 범용적으로 쓸 수 있게 수정해보자.
    - UserDao도 변경한 클래스를 사용하도록 변경하자.
- [ ] 도메인 의존도 제거
    - UserDao에서 추상 클래스를 객체로 만들면 user 객체에 대한 의존도를 제거할 수 있다.
- [ ] 다시 한 번 클래스 추출
    - select도 클래스로 추출 할 수 있지 않을까? SelectJdbcTemplate으로 분리해보자.
    - update, insert와 다르게 ResultSet를 사용해서 데이터 조회 결과를 객체로 매핑해야 한다.
- [ ] 다시 한 번 템플릿 메서드 패턴 적용
    - 분리하고 나니 SelectJdbcTemplate도 JdbcTemplate와 비슷한 부분이 많다. 두 클래스를 다시 합치자.
- [ ] 불필요한 mapRow 메서드 제거
    - update, insert는 mapRow 메서드가 불필요하다. findAll은 setValues 메서드가 불필요하다.
    - 불필요한 구현을 제거하기 위해 mapRow, setValues를 인터페이스로 분리해보자.
- [ ] 라이브러리 확장
    - SQLException은 Checked Exception이다. 커스텀 Exception을 추가해서 사용자는 Unchecked Exception이 되도록 변경하자.
    - RowMapper 인터페이스는 Object를 반환하고 있어서 캐스팅이 사용된다. 제네릭을 사용하도록 개선해보자.
    - 매번 PreparedStatementSetter 인터페이스를 구현하려니 번거롭다. 가변인자를 사용해서 좀더 편리하게 만들 수 없을까?
    - 람다를 적극적으로 활용해서 코드량을 줄여보자.
