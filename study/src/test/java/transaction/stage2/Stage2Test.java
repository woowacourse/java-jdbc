package transaction.stage2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 트랜잭션 전파(Transaction Propagation)란?
 * 트랜잭션의 경계에서 이미 진행 중인 트랜잭션이 있을 때 또는 없을 때 어떻게 동작할 것인가를 결정하는 방식을 말한다.
 *
 * FirstUserService 클래스의 메서드를 실행할 때 첫 번째 트랜잭션이 생성된다.
 * SecondUserService 클래스의 메서드를 실행할 때 두 번째 트랜잭션이 어떻게 되는지 관찰해보자.
 *
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
     * 생성된 트랜잭션이 몇 개인가? 1
     * 왜 그런 결과가 나왔을까? Second(Required)가 First(Required)의 트랜잭션에 참여
     */
    @Test
    void testRequired() {
        final var actual = firstUserService.saveFirstTransactionWithRequired();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithRequired");
    }

    /**
     * 생성된 트랜잭션이 몇 개인가? 2
     * 왜 그런 결과가 나왔을까? Second(RequiredNew)가 First(Required)에 참여하지 않고 트랜잭션을 새로 생성
     */
    @Test
    void testRequiredNew() {
        final var actual = firstUserService.saveFirstTransactionWithRequiredNew();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly(
                        "transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew",
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew"
                );
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는 지 확인해보자.
     */
    @Test
    void testRequiredNewWithRollback() {
        assertThat(firstUserService.findAll()).hasSize(0);

        assertThatThrownBy(() -> firstUserService.saveAndExceptionWithRequiredNew())
                .isInstanceOf(RuntimeException.class);

        assertThat(firstUserService.findAll()).hasSize(1);
    }

    /**
     * FirstUserService.saveFirstTransactionWithSupports() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * supports: 이미 시작된 트랜잭션이 있으면 참여하고, 없으면 트랜잭션없이 진행한다.
     */
    @Test
    void testSupports() {
        final var actual = firstUserService.saveFirstTransactionWithSupports();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithSupports");
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     * mandatory: 트랜잭션이 있으면 참여, 없으면 예외가 발생한다.
     * IllegalTransactionStateException: No existing transaction found for transaction marked with propagation 'mandatory'
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
     * 아래 테스트는 몇 개의 물리적 트랜잭션이 동작할까? 주석 처리 안했을때(required, not supported) 논리적으로는 2개, 물리적으로 1개?
     * FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석 처리하자.
     * 다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이 동작할까? 주석 처리 했을때(x, not supported) 논리적으로는 1개, 물리적으로는 0개?
     * not supported: 현재 트랜잭션이 존재하는 경우 먼저 이를 일시 중지한 다음 트랜잭션 없이 실행한다.
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     * 외부 트랜잭션 범위는 내부 트랜잭션 범위와 논리적으로 독립이지만, 동일한 물리적 트랜잭션에 매핑된다.
     */
    @Test
    void testNotSupported() {

        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported");
    }

    /**
     * 아래 테스트는 왜 실패할까? NestedTransactionNotSupportedException: JpaDialect does not support savepoints - check your JPA provider's capabilities
     *
     * JpaTransactionManager supports nested transactions via JDBC 3.0 Savepoints.
     * The "nestedTransactionAllowed" flag defaults to false though, since nested transactions will just apply to the JDBC Connection, not to the JPA EntityManager and its cached entity objects and related context.
     * You can manually set the flag to true if you want to use nested transactions for JDBC access code which participates in JPA transactions (provided that your JDBC driver supports Savepoints).
     * ❗️Note that JPA itself does not support nested transactions! Hence, do not expect JPA access code to semantically participate in a nested transaction.
     *
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     * nested: 트랜잭션이 존재하는지 확인하고 존재하는 경우 저장 지점을 표시한다.
     *         즉, 비즈니스 로직 실행에서 예외가 발생하면 트랜잭션이 이 저장 지점으로 롤백된다.
     *         활성 트랜잭션이 없으면 REQUIRED 처럼 작동한다.
     *         nested는 Transaction이 존재하면 중첩 Transaction을 만드는 것이다.
     *         중첩 Transaction은 먼저 시작된 부모의 commit과 rollback에는 영향을 받지만, 자신의 commit이나 rollback이 부모의 Transaction에는 영향을 주지 않는 특징이 있다.
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
     * never: 활성 트랜잭션이 있으면 예외가 발생한다.
     * IllegalTransactionStateException: Existing transaction found for transaction marked with propagation 'never'
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
