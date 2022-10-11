# JDBC 라이브러리 구현하기
## 🚀 About the Mission
웹서비스 운영을 위해 사용자 데이터를 메모리가 아닌 DB에 저장할 수 있도록 개선한다.
개발자는 SQL 쿼리 작성, 쿼리에 전달할 인자, SELECT 구문일 경우 조회 결과를 추출하는 것만 집중할 수 있도록 라이브러리를 만들자.

<br>

## 📌 Step 1 - JDBC 라이브러리 구현
- 📝 <b>요구사항</b>
    - 자바 진영에서 제공하는 JDBC API를 활용하여 JDBC 라이브러리 구현하기
- 🖊 <b>구현 목록</b>
    - Dao에서 중복되는 JDBC 로직을 JdbcTemplate으로 분리
    - 함수형 인터페이스를 활용한 RowMapper 구현
    - 템플릿 콜백 패턴을 적용하여 JDBC 로직마다 발생하는 try-catch 중복 코드 제거
    - Mockito 라이브러리를 활용한 JdbcTemplate 테스트 작성
- ✅ <b>체크 리스트</b>
    - [x] UserDaoTest의 모든 테스트 케이스가 통과한다.
    - [x] UserDao가 아닌 JdbcTemplate 클래스에서 JDBC와 관련된 처리를 담당하고 있다.

<br>

## 📌 Step 2 - 리팩토링
- 📝 <b>요구사항</b>
    - 자바가 제공하는 기능을 극한으로 활용해 클린 코드를 작성하는 연습을 한다.
- 🖊 <b>리팩토링에 적용한 자바 기능</b>
    - 함수형 인터페이스
    - 제네릭
    - 가변 인자
    - 람다
    - try-with-resources
- ✅ <b>체크 리스트</b>
    - [x] UserDaoTest의 모든 테스트 케이스가 통과한다.

<br>

## 📌 Step 3 - Transaction 적용하기
- 📝 <b>요구사항</b>
    - UserService 클래스의 changePassword() 메소드에서 원자성을 보장할 수 있도록 트랜잭션을 적용한다.
- 🖊 <b>구현 목록</b>
    - PlatformTransactionManager를 이용한 트랜잭션 적용
    - UserService 추상화를 통한 트랜잭션 서비스와 애플리케이션 서비스의 분리(데코레이터 패턴 적용)
- ✅ <b>체크 리스트</b>
    - [x] 트랜잭션 롤백이 적용되어 UserServiceTest 클래스의 testTransactionRollback() 테스트 케이스가 통과한다.
    - [x] 트랜잭션 서비스와 애플리케이션 서비스가 분리되었다.