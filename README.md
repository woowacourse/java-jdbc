# JDBC 라이브러리 구현하기 🚀 - 3단계

## 미션 설명 ℹ️

User의 비밀번호를 변경할 수 있는 기능을 추가하자. 해당 기능은 UserService 클래스의 changePassword() 메서드에 구현되어있다.

비밀번호를 변경하면 누가, 언제, 어떤 비밀번호로 바꿨는지 이력을 남겨야 한다.

이력이 있어야 고객센터에서 고객 문의를 대응할 수 있다.

고객의 변경 이력을 확인 할 수 있도록 changePassword() 메서드는 비밀번호 변경과 이력을 남기도록 구현되어 있다.

하지만 changePassword() 메서드는 원자성(Atomic)이 보장되지 않는다.

중간에 예외가 발생해서 작업을 완료할 수 없다면 작업을 원래 상태로 되돌려야 한다.

즉, 비밀번호를 바꾸고 이력을 남기는 도중에 에러가 발생하면 원래 비밀번호로 돌려놔야한다.

원자성을 보장하기 위해 트랜잭션을 적용하자.

```java
public class UserService {

    private final UserDao userDao;
    private final UserHistoryDao userHistoryDao;

    public UserService(final UserDao userDao, final UserHistoryDao userHistoryDao) {
        this.userDao = userDao;
        this.userHistoryDao = userHistoryDao;
    }

    public User findById(final long id) {
        return userDao.findById(id);
    }

    public void insert(final User user) {
        userDao.insert(user);
    }

    public void changePassword(final long id, final String newPassword, final String createBy) {
        final var user = findById(id);
        user.changePassword(newPassword);
        userDao.update(user);
        userHistoryDao.log(new UserHistory(user, createBy));
    }
}

```

## 기능 요구 사항 ⚙️

### 1. 트랜잭션 경계 설정하기

JDBC API로 어떻게 트랜잭션을 시작하고 커밋, 롤백을 할 수 있을까? Connection 객체의 setAutoCommit(false) 메서드를 호출하면 트랜잭션이 시작된다. 비즈니스 로직이 끝나면 반드시
트랜잭션 커밋 또는 롤백을 실행한다. 이처럼 트랜잭션을 시작하고 끝나는 부분을 트랜잭션 경계라고 한다.

```java
try(final var connection=dataSource.getConnection();){

        // 트랜잭션 시작
        connection.setAutoCommit(false);

        // 비즈니스 로직 처리
        ...

        // 트랜잭션 커밋
        connection.commit();
        }catch(SQLException e){
        // 트랜잭션 롤백
        // 로직 처리 중에 예외가 발생하면 원자성을 보장하기 위해 롤백한다.
        connection.rollback(); // try-catch로 한 번 더 감싸야 하지만 예시니까 생략
        throw new DataAccessException(e);
        }

```

현재 userDao와 userHistoryDao는 각각 Connection 객체를 만들기 때문에 개별적으로 트랜잭션이 생성된다.

userDao와 userHistoryDao를 한 트랜잭션으로 묶으려면 동일한 Connection 객체를 사용하도록 변경하자.

```java
final var connection=dataSource.getConnection();

// 트랜잭션 시작
        connection.setAutoCommit(false);

        userDao.update(connection,user);
        userHistoryDao.log(connection,new UserHistory(user,createBy));
        ...

```

### 2. 트랜잭션 동기화 적용하기

JdbcTemplate를 구현해서 비즈니스 로직(Service)과 다른 관심사(JDBC)를 깔끔하게 분리했었는데, 트랜잭션을 적용하면서 다시 비즈니스 로직에 JDBC 코드가 섞이게 됐다.

이 문제를 해결하기 위해 스프링에서는 트랜잭션 동기화(Transaction synchronization) 방식을 사용한다.

트랜잭션 동기화란 트랜잭션을 시작하기 위한 Connection 객체를 따로 보관해두고, DAO에서 호출할 때 저장된 Connection을 가져다 사용하는 방식이다.

스프링이 제공하는 PlatformTransactionManager 인터페이스를 활용하여 DAO가 Connection 객체를 파라미터로 전달받아 사용하지 않도록 만들어보자.

```java
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
...
final var transactionManager=new DataSourceTransactionManager(dataSource());

// connection.setAutoCommit(false); 대신에 아래 코드를 실행하면 트랜잭션을 시작한다.
final var transactionStatus=transactionManager.getTransaction(new DefaultTransactionDefinition());
        ...
// 조건에 따라 커밋 또는 롤백이 실행되도록 한다.
        transactionManager.commit(transactionStatus);
        transactionManager.rollback(transactionStatus);

```

JdbcTemplate 클래스에서 Connection 객체를 받아오는 부분은 아래와 같이 수정한다.

```java
import org.springframework.jdbc.datasource.DataSourceUtils;
...
final var connection=DataSourceUtils.getConnection(dataSource);

```

#### 생각해보기

왜 스프링은 PlatformTransactionManager로 트랜잭션을 관리할까?

PlatformTransactionManager 인터페이스는 트랜잭션 동기화 외에 다른 이유로도 사용된다.

로컬 트랜잭션, 글로벌 트랜잭션, JTA 라는 세 가지 키워드와 같이 학습해보자.

### 3. 트랜잭션 서비스 추상화하기

트랜잭션 동기화를 적용하여 DAO에게 Connection 객체를 전달하는 코드를 개선할 수 있었다.

하지만 여전히 UserService에 데이터 액세스와 관련된 로직이 남아있다.

인터페이스를 활용하여 트랜잭션 서비스를 추상화하여 비즈니스 로직과 데이터 액세스 로직을 분리해보자.

먼저 아래와 같은 인터페이스를 추가한다.

```java
public interface UserService {

    User findById(final long id);

    void insert(final User user);

    void changePassword(final long id, final String newPassword, final String createBy);
}

```

그리고 UserService 인터페이스를 구현한 클래스 2개를 만든다.

```java
public class AppUserService implements UserService {
    // 미션 설명에서 제공한 코드를 그대로 사용한다.
}

```

```java
...
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserService implements UserService {

    private final PlatformTransactionManager transactionManager;
    private final UserService userService;

    // override 대상인 메서드는 userService의 메서드를 그대로 위임(delegate)한다.

    @Override
    public void changePassword(final long id, final String newPassword, final String createBy) {
        // 트랜잭션 처리 영역

        userService.changePassword(id, newPassword, createBy);

        // 트랜잭션 처리 영역
    }
}

```

UserServiceTest 클래스의 testTransactionRollback() 테스트 메서드를 아래와 같이 바꿔보자.

그리고 테스트가 통과하도록 만들자.

```java
@Test
void testTransactionRollback(){
// 트랜잭션 롤백 테스트를 위해 mock으로 교체
final var userHistoryDao=new MockUserHistoryDao(jdbcTemplate);
// 애플리케이션 서비스
final var appUserService=new AppUserService(userDao,userHistoryDao);
// 트랜잭션 서비스 추상화
final var transactionManager=new DataSourceTransactionManager(jdbcTemplate.getDataSource());
final var userService=new TxUserService(transactionManager,appUserService);

final var newPassword="newPassword";
final var createBy="gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
        ()->userService.changePassword(1L,newPassword,createBy));

final var actual=userService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
        }

```

## 체크리스트 👍

- [x] 트랜잭션 롤백이 적용되어 UserServiceTest 클래스의 testTransactionRollback() 테스트 케이스가 통과한다.
- [x] 트랜잭션 서비스와 애플리케이션 서비스가 분리되었다.
- [ ] 로컬 트랜잭션, 글로벌 트랜잭션, JTA 공부하기

## 피드백

- [x] setObject null/wrapper 값 검증
- [x] 줄바꿈 수정, 주석 삭제, 기본 생성자 삭제
- [x] connection 연결 관련 코드 추가
