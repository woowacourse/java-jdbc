package transaction.stage2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 트랜잭션 전파(Transaction Propagation)란?
 * 트랜잭션의 경계에서 이미 진행 중인 트랜잭션이 있을 때 또는 없을 때 어떻게 동작할 것인가를 결정하는 방식을 말한다.
 * <p>
 * FirstUserService 클래스의 메서드를 실행할 때 첫 번째 트랜잭션이 생성된다.
 * SecondUserService 클래스의 메서드를 실행할 때 두 번째 트랜잭션이 어떻게 되는지 관찰해보자.
 * <p>
 * https://docs.spring.io/spring-framework/docs/current/reference/html/data-access.html#tx-propagation
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Stage2Test {

    private static final Logger log = LoggerFactory.getLogger(Stage2Test.class);

    @Autowired
    private FirstUserService firstUserService;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    /**
     * 생성된 트랜잭션이 몇 개인가?
     * <p>
     * 2024-10-10T13:32:39.792+09:00  INFO 4320 --- [           main] transaction.stage2.FirstUserService      :
     * transaction.stage2.FirstUserService.saveFirstTransactionWithRequired is Actual Transaction Active : ✅ true
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-10T13:32:39.795+09:00  INFO 4320 --- [           main] transaction.stage2.SecondUserService     :
     * transaction.stage2.FirstUserService.saveFirstTransactionWithRequired is Actual Transaction Active : ✅ true
     * <p>
     * 왜 그런 결과가 나왔을까?
     * <p>
     * `@Transactional(propagation = Propagation.REQUIRED)` 설정으로 인해서.
     * 기본적으로 해당 메써드를 호출한 곳에서 별도의 트랜잭션이 설정되어 있지 않았다면 트랜잭션를 새로 시작한다.
     * (새로운 연결을 생성하고 실행한다.) 만약, 호출한 곳에서 이미 트랜잭션이 설정되어 있다면 새로운 트랜잭션을 실행한다.
     * (동일한 연결 안에서 실행된다.) 예외가 발생하면 롤백이 되고 호출한 곳에도 롤백이 전파된다.
     */
    @Test
    void testRequired() {
        final var actual = firstUserService.saveFirstTransactionWithRequired();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithRequired");
    }

    /**
     * 생성된 트랜잭션이 몇 개인가?
     * <p>
     * transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew is Actual Transaction Active : ✅ true
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-10T13:57:22.697+09:00  INFO 20640 --- [           main] transaction.stage2.SecondUserService     :
     * transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew is Actual Transaction Active : ✅ true
     * <p>
     * 왜 그런 결과가 나왔을까?
     * <p>
     * `@Transactional(propagation = Propagation.REQUIRES_NEW)` 설정으로 인해서.
     * 기본적으로 해당 메써드를 호출한 곳에서 별도의 트랜잭션를 새로 시작한다.
     * (새로운 연결을 생성하고 실행한다.) 호출한 곳에서 이미 트랜잭션이 설정되어 있든 없든 기존의 새로운 트랙잭션을 실행한다.
     */
    @Test
    void testRequiredNew() {
        final var actual = firstUserService.saveFirstTransactionWithRequiredNew();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew",
                        "transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew");
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
     * <p>
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는지 확인해보자.
     * <p>
     * REQUIRES_NEW 경우는 호출한 곳에서 이미 트랜잭션이 설정되어 있어도 새로운 트랜잭션을 실행한다.
     * 그래서 secondUserService.saveSecondTransactionWithRequiresNew()와
     * firstUserService.saveAndExceptionWithRequiredNew()는 다른 트랜잭션이다.
     * 그래서 firstUserService.saveAndExceptionWithRequiredNew()에서 발생한 에러는
     * secondUserService.saveSecondTransactionWithRequiresNew()의 롤백에는 영향을 주지 않는다.
     * 그래서 secondUserService.saveSecondTransactionWithRequiresNew()는 정상 실행되므로 1개의 데이터가 저장된다.
     */
    @Test
    void testRequiredNewWithRollback() {
        assertThat(firstUserService.findAll()).isEmpty();

        assertThatThrownBy(() -> firstUserService.saveAndExceptionWithRequiredNew())
                .isInstanceOf(RuntimeException.class);

        assertThat(firstUserService.findAll()).hasSize(1);
    }

    /**
     * FirstUserService.saveFirstTransactionWithSupports() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * <p>
     * SUPPORTS란?
     * 현재 트랜잭션을 지원하고, 트랜잭션이 없는 경우 비트랜잭션으로 실행합니다. 같은 이름의 EJB 트랜잭션 속성과 유사합니다.
     * 참고: 트랜잭션 동기화가 있는 트랜잭션 관리자의 경우, SUPPORTS는 동기화가 적용될 트랜잭션 범위를 정의하므로 트랜잭션이 전혀 없는 것과는 약간 다릅니다.
     * 결과적으로 지정된 전체 범위에 대해 동일한 리소스(JDBC 연결, 최대 절전 모드 세션 등)가 공유됩니다.
     * <p>
     * (주석이 있을 경우)
     * SecondUserService.saveSecondTransactionWithSupports이 Propagation.SUPPORTS 임으로
     * 부모 트랜잭션이 없으면 비트랜잭션으로 실행합니다.
     * 하지만 트랜잭션이 동작하긴한다.
     * 그래서 transaction.stage2.SecondUserService.saveSecondTransactionWithSupports가 실행된 것이다.
     * <p>
     * (주석이 없을 경우)
     * SecondUserService.saveSecondTransactionWithSupports이 Propagation.SUPPORTS 임으로
     * 부모 트랜잭션이 있으면 해당 트랜잭션을 사용한다.
     * transaction.stage2.FirstUserService.saveFirstTransactionWithSupports가 실행된 것이다.
     */
    @Test
    void testSupports() {
        final var actual = firstUserService.saveFirstTransactionWithSupports();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithSupports");
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     * <p>
     * MANDATORY란?
     * 현재 트랜잭션을 지원하고, 존재하지 않으면 예외를 던집니다. 같은 이름의 EJB 트랜잭션 속성과 유사합니다.
     * <p>
     * (주석이 있을 경우)
     * <p>
     * 2024-10-10T14:25:01.422+09:00  INFO 24660 --- [           main] transaction.stage2.FirstUserService      :
     * null is Actual Transaction Active : ❌ false
     * <p>
     * SecondUserService.saveSecondTransactionWithSupports이 Propagation.MANDATORY 임으로
     * 부모 트랜잭션이 없으면 예외를 발생시킨다.
     * <p>
     * (주석이 없을 경우)
     * 2024-10-10T14:28:19.295+09:00  INFO 17608 --- [           main] transaction.stage2.FirstUserService      :
     * transaction.stage2.FirstUserService.saveFirstTransactionWithMandatory is Actual Transaction Active : ✅ true
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-10T14:28:19.295+09:00  INFO 17608 --- [           main] transaction.stage2.SecondUserService     :
     * transaction.stage2.FirstUserService.saveFirstTransactionWithMandatory is Actual Transaction Active : ✅ true
     * <p>
     * SecondUserService.saveSecondTransactionWithSupports이 Propagation.MANDATORY 임으로
     * 부모 트랜잭션이 있으면 해당 트랜잭션을 사용한다.
     * transaction.stage2.FirstUserService.saveFirstTransactionWithSupports가 실행된 것이다.
     */
    @Test
    void testMandatory() {
        final var actual = firstUserService.saveFirstTransactionWithMandatory();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithMandatory");
    }

    /**
     * 아래 테스트는 JPA를 사용한다는 가정 하에 몇 개의 물리적 트랜잭션이 동작할까?
     * <p>
     * saveFirstTransactionWithNotSupported()는 Propagation.REQUIRED로 설정되어 있기 때문에, 이 메서드가 호출될 때 하나의 물리적 트랜잭션이 시작됩니다.
     * saveSecondTransactionWithNotSupported()는 Propagation.NOT_SUPPORTED로 설정되어 있으므로 현재 트랜잭션이 일시 중단되고,
     * 이 메서드는 트랜잭션 없이 실행됩니다.
     * 따라서, 이 경우 1개의 물리적 트랜잭션이 발생한다
     * <p>
     * FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석 처리하자.
     * 다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이 동작할까?
     * <p>
     * saveFirstTransactionWithNotSupported() 메서드에서 @Transactional을 주석 처리하면, 이 메서드는 명시적인 트랜잭션을 시작하지 않습니다.
     * 따라서, 이 경우 0개의 물리적 트랜잭션이 필수로 발생한다.
     * <p>
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     * <p>
     * 물리적 트랜잭션 (Physical Transaction): 데이터베이스와 같은 외부 자원에서 실제로 발생하는 트랜잭션으로,
     * 커밋과 롤백을 통해 데이터베이스 상태를 변경하는 가장 기본적인 단위입니다. JPA 컨텍스트에서, 물리적 트랜잭션은
     * 실제 데이터베이스 연결과 관련되며, 영속성 컨텍스트의 변경사항을 데이터베이스에 반영하는 과정을 포함합니다.
     * <p>
     * 논리적 트랜잭션 (Logical Transaction): 애플리케이션 코드에서 트랜잭션 경계를 논리적으로 나누는 단위로,
     * 물리적 트랜잭션과는 별개로 동작할 수 있으며 여러 논리적 트랜잭션이 하나의 물리적 트랜잭션 내에서 발생할 수 있습니다.
     * JPA와 스프링을 함께 사용할 때, 논리적 트랜잭션은 주로 @Transactional 어노테이션으로 정의되며,
     * 이는 JPA의 영속성 컨텍스트 라이프사이클과 밀접하게 연관됩니다.
     */
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported",
                        "transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported");
    }

    /**
     * `@Transactional(propagation = Propagation.NESTED)`는
     * 현재 트랜잭션이 존재하면 중첩된 트랜잭션 내에서 실행하고, 그렇지 않으면 REQUIRED처럼 동작합니다.
     * <p>
     * 아래 테스트는 왜 실패할까?
     * <p>
     * h2는 세이브포인트 기능을 지원하지 않기 때문이다. (JpaDialect does not support savepoints - check your JPA provider's capabilities)
     * <p>
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     * <p>
     * 일반 REQUIRED처럼 동작합니다.
     */
    @Test
    void testNested() {
        final var actual = firstUserService.saveFirstTransactionWithNested();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNested");
    }

    /**
     * 마찬가지로 @Transactional을 주석처리하면서 관찰해보자.
     * <p>
     * 주석을 하지 않을 경우, 트랜잭션이 있는 메서드가 @Transactional(propagation = Propagation.NEVER)을 호출하는 것으로 예외가 발생한다.
     * 주석을 할 경우, 트랜잭션이 있는 메서드가 @Transactional(propagation = Propagation.NEVER)을 트랜잭션이 없는 메서드로 예외가 발생하지 않고 정상 작동한다.
     */
    @Test
    void testNever() {
        final var actual = firstUserService.saveFirstTransactionWithNever();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNever");
    }
}
