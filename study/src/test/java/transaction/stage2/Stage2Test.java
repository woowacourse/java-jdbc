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
 * 트랜잭션 전파(Transaction Propagation)란? 트랜잭션의 경계에서 이미 진행 중인 트랜잭션이 있을 때 또는 없을 때 어떻게 동작할 것인가를 결정하는 방식을 말한다.
 * <p>
 * FirstUserService 클래스의 메서드를 실행할 때 첫 번째 트랜잭션이 생성된다. SecondUserService 클래스의 메서드를 실행할 때 두 번째 트랜잭션이 어떻게 되는지 관찰해보자.
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
     * 생성된 트랜잭션이 몇 개인가? 왜 그런 결과가 나왔을까? -> 1개이다. 기존 트랜잭션이 있으면 그 트랜잭션을 사용하고, 없다면 새로운 트랜잭션을 생성합니다.
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
     * 생성된 트랜잭션이 몇 개인가? 왜 그런 결과가 나왔을까? 2개 -> 항상 새로운 물리적 트랜잭션을 생성합니다.
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
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다. REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는
     * 지 확인해보자. firstUserService의 트랜잭션은 rollback 되었으므로, member가 저장되지 못했다. secondUserService의 트랜잭션은 별개의 트랜잭션으로, 정상적으로
     * commit되어 member가 성공적으로 저장되었다.
     */
    @Test
    void testRequiredNewWithRollback() {
        assertThat(firstUserService.findAll()).isEmpty();

        assertThatThrownBy(() -> firstUserService.saveAndExceptionWithRequiredNew())
                .isInstanceOf(RuntimeException.class);

        assertThat(firstUserService.findAll()).hasSize(1);
    }

    /**
     * FirstUserService.saveFirstTransactionWithSupports() 메서드를 보면 @Transactional이 주석으로 되어 있다. 주석인 상태에서 테스트를 실행했을 때와 주석을
     * 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * - @Transaction 있는 경우: saveFirstTransactionWithSupports가 담겨있으며, 트랜잭션이 두 메서드에서 모두 active되어 있다.
     * -> 트랜잭션이 존재한다면, 그대로 사용하므로 첫번째 메서드에서 생성된 트랜잭션이 담긴다.
     * - @Transaction 없는 경우:: active되어있는 트랜잭션이 존재하지 않는다.
     * -> 기존 트랜잭션이 없으면 물리적 트랜잭션이 생성되지 않는다.
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
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다. 주석인 상태에서 테스트를 실행했을 때와
     * 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자. SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     * <p>
     * - @Transaction 있는 경우: 두 메서드에서 모두 트랜잭션이 active되어 있으며, saveFirstTransactionWithMandatory이 담긴다. -> Mandatory 옵션은 기존 트랜잭션을 강제 시킨다.
     * - @Transaction 없는 경우: No existing transaction found for transaction marked with propagation 'mandatory' 예외 발생
     * 기존 트랜잭션이 존재하지 않는다면 예외를 발생시킨다. -> Suppors 옵션은 기존 트랜잭션이 없으면, 트랜잭션 없이 동작한다는점이 다르다.
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
     * 아래 테스트는 몇 개의 물리적 트랜잭션이 동작할까? FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석
     * 처리하자. 다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이 동작할까?
     * <p>
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     * - @Transaction 있는 경우: 물리적 트랜잭션은 하나만 동작한다.
     * - @Transaction 없는 경우: 물리적 트랜잭션은 존재하지 않는다.
     * -> NotSupported는 항상 트랜잭션 없이 동작한다. 기존 트랜잭션이 존재해도, 기존 것을 중지시키고 동작한다.
     */
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported",
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported");
    }

    /**
     * 아래 테스트는 왜 실패할까? FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     * - NestedTransactionNotSupportedException이 발생한다. hibernate는(HibernateTransactionManager) savepoint 기능을 제공하지 않는다.
     * - NestedTransaction은 기존 트랜잭션 내에서 하위 트랜잭션(내부 트랜잭션)을 생성한다. 존재하지 않는다면, 새로운 트랜잭션을 시작한다.
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
     * 1. @Transactional 있는 경우: Existing transaction found for transaction marked with propagation 'never'
     * 2. @Transactional 없는 경우: 트랜잭션 없이 수행된다.
     * -> 기존 트랜잭션이 있으면 예외를 발생시키며, 트랜잭션 없이 실행해야 한다.
     */
    @Test
    void testNever() {
        final var actual = firstUserService.saveFirstTransactionWithNever();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithNever");
    }
}
