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



-------

Step 2

1. SQLException은 Checked Exception이다. 커스텀 Exception을 추가해서 사용자는 Unchecked Exception이 되도록 변경하자.
기존에는 Checked Exception인 SQLException을 catch하고 RuntimeException을 반환했다.
  
이를 RuntimeException을 상속받은 DataAccessException으로 교체해주었다.

*Unchecked Exception은 RuntimeException을 상속받은 모든 예외로 컴파일 시점에 에러가 터지지 않는다. 
*Checked Exception은 컴파일 시점에 에러가 터진다. 

2. RowMapper 인터페이스는 Object를 반환하고 있어서 캐스팅이 사용된다. 제네릭을 사용하도록 개선해보자.
이미 Step 1에서 제너릭 타입을 사용하도록 코드를 수정했었다. (RowMapper.java 참고)
따라서, Step 2에서 변경은 없다. 

3. 매번 PreparedStatementSetter 인터페이스를 구현하려니 번거롭다. 가변인자를 사용해서 좀더 편리하게 만들 수 없을까?
이미 Step 1에서 Object ...으로 인자를 받았기 때문에, Step 2에서 변경은 없다. 
따라서, Step 2에서 변경은 없다.

4. 람다를 적극적으로 활용해서 코드량을 줄여보자.
이미 RowMapper에서 mapRow를 rs -> new User와 같이 람다 식으로 구현해 두었다. (UserDao.java 참고)
따라서, Step 2에서 변경은 없다.


5. JDBC 라이브러리	vs 개발자


------


Step 4

1. `UserService`에서 JDBC 연결을 `DataSourceUtils`로 반환하려면 왜 `try-with-resources`를 `finally`로 바꿔야 할까?

   기존 `try-with-resources`는 블록이 끝날 때 `Connection.close()`를 직접 호출한다.
   `UserService.changePassword()`는 성공하거나 예외가 발생해도 연결 반환을 `DataSourceUtils.releaseConnection()`에 맡기기 위해 `finally`로 바꿨다.
   트랜잭션 동기화를 적용하면 이 메서드에서 공유 중인 연결을 트랜잭션이 끝날 때까지 열어둘 수 있다.
