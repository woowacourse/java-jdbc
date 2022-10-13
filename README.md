# JDBC 라이브러리 구현하기
## 1단계 - JDBC 라이브러리 구현하기
- [x] UserDaoTest의 모든 테스트 케이스가 통과한다.
  - [x] update메서드를 구현한다.
  - [x] findAll메서드를 구현한다.
  - [x] findByAccount메서드를 구현한다.
- [x] UserDao가 아닌 JdbcTemplate 클래스에서 JDBC와 관련된 처리를 담당하고 있다.
  - [x] update메서드를 구현한다.
  - [x] query메서드를 구현한다.
  - [x] queryForObject메서드를 구현한다.


## 2단계 - 리팩터링
- DataAccessUtils분리
- final 키워드 추가

## 3단계 - Transaction 적용하기
- [x] 트랜잭션 롤백이 적용되어 UserServiceTest 클래스의 testTransactionRollback() 테스트 케이스가 통과한다.
- [x] 트랜잭션 서비스와 애플리케이션 서비스가 분리되었다.


## PlatformTransactionManager
PlatformTransactionManager은 인터페이스이기 때문에 필요하다면 쉽게 모킹하거나 스터빙할 수 있다.
이는 JNDI같은 검색 전략에 의존하지 않는다.

PlatformTransactionManager 구현체는 스프링 프레임워크 IoC 컨테이너의 다른 객체(또는 빈)처럼 정의한다.
이러한 이점은 JTA로 작업할때 조차도 스프링 프레임워크 트랜잭션을 훌륭한 추상화로 만들어준다. 
트랜잭션이 적용된 코드는 JTA를 직접 사용하는 것보다 훨씬 쉽게 테스트할 수 있다.

다시 스프링 철학과 일치하도록 PlatformTransactionManager 인터페이스의 어떤 메서드라도 던질 수 있는 TransactionException은 언체크드(unchecked)이다. 
(즉, 이는 java.lang.RuntimeException 클래스를 확장한다.) 트랜잭션 인프라스트럭처의 실패는 거의 예외없이 치명적이다. 
애플리케이션 코드가 트랜잭션 실패를 실제로 복구할 수 있는 드문 경우에 애플리케이션 개발자는 여전히 TransactionException를 잡아서 다룰 수 있다. 
개발자가 이렇게 하도록 강제하지 않는다는 것이 두드러진 점이다.

getTransaction(..) 메서드는 TransactionDefinition 파라미터에 따라 TransactionStatus 객체를 반환한다. 
반환된 TransactionStatus는 새로운 트랜잭션을 나타내거나 현재 콜스택에 존재하는 트랜잭션 중 일치하는 것이 있다면 존재하는 트랜잭션을 나타날 수 있다. 
후자의 경우는 Java EE 트랜잭션 컨텍스트처럼 TransactionStatus가 실행 스레드와 연결되었다는 것을 암시한다.  

TransactionDefinition 인터페이스는 다음을 지정한다.

+ **격리(Isolation)**: 해당 트랜잭션이 다른 트랜잭션의 작업과 격리되는 정도. 예를 들어 해당 트랜잭션이 다른 트랜잭션에서 아직 커밋되지 않은 쓰기작업을 볼 수 있는가?
+ **전파(Propagation)**: 보통 트랜잭션 범위내에서 실행되는 모든 코드는 해당 크랜잭션에서 실행될 것이다. 하지만 트랜잭션 컨텍스트가 이미 존재하는 경우 트랜잭션이 적용된 메서드가 실행할 때의 동작을 지정하는 옵션이 있다. 예를 들어 코드를 존재하는 트랜잭션 (일반적인 경우)에서 계속 실행할 수 있다. 또는 존재하는 트랜잭션을 일시정지하고 새로운 트랜잭션을 생성할 수도 있다. 스프링은 EJB CMT에서 익숙한 모든 트랜잭션 전파 옵션을 제공한다. 
+ **시간만료(Timeout)**: 시간이 만료되기 전에 해당 트랜잭션이 얼마나 오랫동안 실행되고 의존 트랜잭션 인프라스트럭처가 자동으로 롤백하는 지를 나타낸다.
+ **읽기 전용 상태(Read-only status)**: 코드가 데이터를 읽기는 하지만 수정하지는 않는 경우 읽기 전용 트랜잭션을 사용할 수 있다. 읽기 전용 트랜잭션은 하이버네이트를 사용하는 경우처럼 몇가지 경우에 유용한 최적화가 될 수 있다.


스프링에서 선언적인 트랜잭션 관리나 프로그래밍적인 트랜잭션 관리 중에 어느 것을 선택했는지에 관계없이 제대로된 PlatformTransactionManager 구현체를 정의하는 것이 정말로 가장 중요하다. 
보통은 의존성 주입으로 이 구현체를 정의한다.

PlatformTransactionManager 구현체는 일반적으로 JDBC, JTA, Hibernate 등과 같은 동작하는 환경에 대한 지식을 필요로 한다.

