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

## 🚀 1단계 - JDBC 라이브러리 구현하기

### 기능 요구 사항

- [x] 개발자는 SQL 쿼리 작성, 쿼리에 전달할 인자, SELECT 구문일 경우 조회 결과를 추출하는 것만 집중할 수 있도록 라이브러리를 만들어야 한다.

> 리팩터링은 UserDaoTest를 활용해 진행한다.
>
> 중복을 제거하기 위한 라이브러리는 JdbcTemplate 클래스에 구현한다.
>
> DataSource는 DataSourceConfig 클래스의 getInstance() 메서드를 호출하면 된다.

## 🚀 2단계 - 리팩터링

- 미션 설명) 자바가 제공하는 기능을 극한으로 활용해 클린 코드를 작성하는 연습을 한다.
    - 익명 클래스 / 함수형 인터페이스 / 제네릭 / 가변 인자 / 람다 / try-with-resources / checked vs unchecked exception

### 기능 요구 사항

- [x] 아래의 동작들에 맞춰 코드를 리팩터링 한다.
    - 개발자의 동작:
        - 연결 매개변수 설정
        - SQL문 작성
        - SQL문의 매개변수와 값 설정
        - Result Set에서 데이터 추출
    - JDBC 라이브러리의 동작:
        - Connection 생성
        - Statement 준비 및 실행
        - ResultSet 생성
        - 예외처리
        - 트랜젝션 관리
        - Connection, Statement, ResultSet 객체 close

## 🚀 3단계 - Transaction 적용하기

- [x] User의 비밀번호를 변경할 수 있는 기능을 추가한다.
    - 해당 기능은 UserService 클래스의 changePassword() 메서드에 구현되어있다.
    - 비밀번호를 변경하면 누가, 언제, 어떤 비밀번호로 바꿨는지 이력을 남겨야 한다.
    - changePassword() 메서드는 비밀번호 변경과 이력을 남기도록 구현되어 있다.

    - 하지만 changePassword() 메서드는 원자성(Atomic)이 보장되지 않는다.
    - 중간에 예외가 발생해서 작업을 완료할 수 없다면 작업을 원래 상태로 되돌려야 한다.
    - 즉, 비밀번호를 바꾸고 이력을 남기는 도중에 에러가 발생하면 원래 비밀번호로 돌려놔야 한다.
    - 원자성을 보장하기 위해 트랜잭션을 적용해야 한다.

    - 현재 userDao와 userHistoryDao는 각각 Connection 객체를 만들기 때문에 개별적으로 트랜잭션이 생성된다.
    - userDao와 userHistoryDao를 한 트랜잭션으로 묶으려면 동일한 Connection 객체를 사용하도록 변경해야 한다.

## 🚀 4단계 - Transaction synchronization 적용하기

UserService에서 changePassword() 메서드를 하나의 트랜잭션으로 처리하려면 Connection 객체가 비즈니스 로직과 섞이게 된다.
이 문제를 해결하기 위해 트랜잭션 동기화(Transaction synchronization) 방식을 사용해보자.
트랜잭션 동기화란 트랜잭션을 시작하기 위한 Connection 객체를 따로 보관해두고, DAO에서 호출할 때 저장된 Connection을 가져다 사용하는 방식이다.
DataSourceUtils와 TransactionSynchronizationManager를 활용하여 DAO가 Connection 객체를 파라미터로 전달받아 사용하지 않도록 만들어보자.

- [ ] Transaction synchronization 적용하기
  서비스와 DAO에서 Connection 객체를 가져오는 부분은 DataSourceUtils를 사용하도록 수정하자.
  그리고 TransactionSynchronizationManager 클래스가 올바르게 작동하도록 구현해보자.

    ```java
    public void changePassword(final long id, final String newPassword, final String createdBy) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        connection.setAutoCommit(false);
    
        try {
            // todo
            connection.commit();
        } catch (...){
            connection.rollback();
            ...
        } finally{
            DataSourceUtils.releaseConnection(connection, dataSource);
            TransactionSynchronizationManager.unbindResource(dataSource);
        }
    }
    ```

  생각해보기 🤔
  JDBC가 아닌 JPA, JMS 같이 다른 커넥션을 사용하거나 2개 이상의 데이터소스를 하나의 트랜잭션처럼 관리하려면 어떻게 해야 할까?
  스프링에서는 이 문제를 PlatformTransactionManager를 사용하여 해결한다.
  PlatformTransactionManager가 어떻게 추상화되어 있는지는 스프링 문서를 참고하자.
  추가로 로컬 트랜잭션, 글로벌 트랜잭션, JTA 라는 세 가지 키워드도 같이 학습하자.

- [ ] 트랜잭션 서비스 추상화하기
  트랜잭션 동기화를 적용하여 DAO에게 Connection 객체를 전달하는 코드를 개선할 수 있었다.
  하지만 여전히 UserService에 데이터 액세스와 관련된 로직이 남아있다.
  인터페이스를 활용하여 트랜잭션 서비스를 추상화하여 비즈니스 로직과 데이터 액세스 로직을 분리해보자.
  먼저 아래와 같은 인터페이스를 추가한다.

    ```java
    public interface UserService {
    
        User findById(final long id);
    
        void save(final User user);
    
        void changePassword(final long id, final String newPassword, final String createdBy);
    }
    ```

  그리고 UserService 인터페이스를 구현한 클래스 2개를 만든다.

    ```java
    public class AppUserService implements UserService {
        // todo
    }
    ```

    ``` java
    public class TxUserService implements UserService {
    
        private final UserService userService;
    
        // override 대상인 메서드는 userService의 메서드를 그대로 위임(delegate)한다.
        @Override
        public void changePassword(final long id, final String newPassword, final String createdBy) {
            // 트랜잭션 처리 영역
    
            userService.changePassword(id, newPassword, createdBy);
    
            // 트랜잭션 처리 영역
        }
    }
    ```

- [ ] UserServiceTest 클래스의 testTransactionRollback() 테스트 메서드를 아래와 같이 바꿔보자.
  그리고 테스트가 통과하도록 만들자.

  테스트 코드를 통과시키고 미션을 마무리한다.

    ```java
  @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        // 애플리케이션 서비스
        final var appUserService = new AppUserService(userDao, userHistoryDao);
        // 트랜잭션 서비스 추상화
        final var userService = new TxUserService(appUserService);
    
        final var newPassword = "newPassword";
        final var createdBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createdBy));
    
        final var actual = userService.findById(1L);
    
        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
    ```
