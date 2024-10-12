package transaction.stage2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
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
     * 왜 그런 결과가 나왔을까?
     */
    @DisplayName("Propagation.REQUIRED의 경우 진행 중인 트랜잭션이 없으면 새로 시작하고, 있다면 참여한다.")
    @Test
    void testRequired() {
        final var actual = firstUserService.saveFirstTransactionWithRequired();
        // transaction.stage2.FirstUserService: transaction.stage2.FirstUserService.saveFirstTransactionWithRequired is Actual Transaction Active : ✅ true
        // transaction.stage2.SecondUserService: transaction.stage2.FirstUserService.saveFirstTransactionWithRequired is Actual Transaction Active : ✅ true
        // 같은 트랜잭션을 사용한다.

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithRequired");
    }

    /**
     * 생성된 트랜잭션이 몇 개인가?
     * 왜 그런 결과가 나왔을까?
     */
    @DisplayName("Propagation.REQUIRES_NEW의 경우 진행 중인 트랜잭션이 있어도 항상 새로운 트랜잭션을 시작한다.")
    @Test
    void testRequiredNew() {
        final var actual = firstUserService.saveFirstTransactionWithRequiredNew();
        // transaction.stage2.FirstUserService: transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew is Actual Transaction Active : ✅ true
        // transaction.stage2.SecondUserService: transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew is Actual Transaction Active : ✅ true
        // 다른 트랜잭션을 사용한다.

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew",
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew");
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는 지 확인해보자.
     */
    @DisplayName("Propagation.REQUIRES_NEW의 경우 예외가 발생한 트랜잭션만 롤백한다.")
    @Test
    void testRequiredNewWithRollback() {
        assertThat(firstUserService.findAll()).hasSize(0);

        assertThatThrownBy(() -> firstUserService.saveAndExceptionWithRequiredNew())
                .isInstanceOf(RuntimeException.class);
        // transaction.stage2.SecondUserService: transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew is Actual Transaction Active : ✅ true
        // transaction.stage2.FirstUserService: transaction.stage2.FirstUserService.saveAndExceptionWithRequiredNew is Actual Transaction Active : ✅ true

        assertThat(firstUserService.findAll()).hasSize(1);
    }

    /**
     * FirstUserService.saveFirstTransactionWithSupports() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     */
    @DisplayName("Propagation.SUPPORTS의 경우 기존에 생성된 중인 Transaction 이 있을 때만 참여하고 없다면 Transaction 없이 진행한다.")
    @Test
    void testSupports() {
        final var actual = firstUserService.saveFirstTransactionWithSupports();
        // 주석
        // transaction.stage2.FirstUserService: null is Actual Transaction Active : ❌ false
        // transaction.stage2.SecondUserService: transaction.stage2.SecondUserService.saveSecondTransactionWithSupports is Actual Transaction Active : ❌ false

        // 주석 해제
        // transaction.stage2.FirstUserService: transaction.stage2.FirstUserService.saveFirstTransactionWithSupports is Actual Transaction Active : ✅ true
        // transaction.stage2.SecondUserService: transaction.stage2.FirstUserService.saveFirstTransactionWithSupports is Actual Transaction Active : ✅ true
        log.info("transactions : {}", actual);
        // 주석
        // transactions : [transaction.stage2.SecondUserService.saveSecondTransactionWithSupports]

        // 주석 해제
        // transactions : [transaction.stage2.FirstUserService.saveFirstTransactionWithSupports]
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithSupports");
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자. -> 트랜잭션 없으면 예외 발생
     */
    @DisplayName("Propagation.MANDATORY의 경우 현재 트랜잭션을 지원하고, 존재하지 않는 경우 예외가 발생한다.")
    @Test
    void testMandatory() {
        final var actual = firstUserService.saveFirstTransactionWithMandatory();
        // 주석
        // transaction.stage2.FirstUserService: null is Actual Transaction Active : ❌ false
        // 예외 -> org.springframework.transaction.IllegalTransactionStateException: No existing transaction found for transaction marked with propagation 'mandatory'

        // 주석 해제
        // transaction.stage2.FirstUserService: transaction.stage2.FirstUserService.saveFirstTransactionWithMandatory is Actual Transaction Active : ✅ true
        // transaction.stage2.SecondUserService: transaction.stage2.FirstUserService.saveFirstTransactionWithMandatory is Actual Transaction Active : ✅ true

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithMandatory");
    }

    /**
     * 아래 테스트는 몇 개의 물리적 트랜잭션이 동작할까? -> 2개
     * FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석 처리하자.
     * 다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이 동작할까? -> 1개
     * <p>
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     * 물리 : 실제 데이터베이스의 트랜잭션과 상응하는 개념
     * 논리 : 스프링의 트랜잭션 관리 메커니즘인 PlatformTransactionManager에 의해 관리되는 트랜잭션 단위
     */
    @DisplayName("Propagation.NOT_SUPPORTED의 경우 비트랜잭션으로 실행하고, 현재 트랜잭션이 있는 경우 일시 중단한다.")
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();
        // 주석 해제
        // transaction.stage2.FirstUserService: transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported is Actual Transaction Active : ✅ true
        // transaction.stage2.SecondUserService: transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported is Actual Transaction Active : ❌ false

        // 주석
        // transaction.stage2.FirstUserService: null is Actual Transaction Active : ❌ false
        // transaction.stage2.SecondUserService: transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported is Actual Transaction Active : ❌ false

        log.info("transactions : {}", actual);
        // 주석 해제
        // transactions : [transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported, transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported]

        // 주석
        // transactions : [transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported]
        assertThat(actual)
                .hasSize(2)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported", "transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported");
    }

    /**
     * 아래 테스트는 왜 실패할까?
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     */
    @DisplayName("Propagation.NESTED의 경우 현재 트랜잭션이 있는 경우 중첩된 트랜잭션 내에서 실행하고, 그렇지 않으면 REQUIRED처럼 동작한다.")
    @Test
    void testNested() {
        final var actual = firstUserService.saveFirstTransactionWithNested();
        // 주석 해체
        // transaction.stage2.FirstUserService: transaction.stage2.FirstUserService.saveFirstTransactionWithNested is Actual Transaction Active : ✅ true
        // 예외 -> org.springframework.transaction.NestedTransactionNotSupportedException: JpaDialect does not support savepoints - check your JPA provider's capabilities

        // 주석
        // transaction.stage2.FirstUserService: null is Actual Transaction Active : ❌ false
        // transaction.stage2.SecondUserService: transaction.stage2.SecondUserService.saveSecondTransactionWithNested is Actual Transaction Active : ✅ true
        log.info("transactions : {}", actual);

        // 주석
        // transactions : [transaction.stage2.SecondUserService.saveSecondTransactionWithNested]
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNested");
    }

    /**
     * 마찬가지로 @Transactional을 주석처리하면서 관찰해보자.
     */
    @DisplayName("Propagation.NEVER의 경우 트랜잭션 없이 실행하고, 트랜잭션이 존재하면 예외가 발생한다.")
    @Test
    void testNever() {
        final var actual = firstUserService.saveFirstTransactionWithNever();
        // 주석 해제
        // transaction.stage2.FirstUserService: transaction.stage2.FirstUserService.saveFirstTransactionWithNever is Actual Transaction Active : ✅ true
        // 예외 -> org.springframework.transaction.IllegalTransactionStateException: Existing transaction found for transaction marked with propagation 'never'

        // 주석
        // transaction.stage2.FirstUserService: null is Actual Transaction Active : ❌ false
        // transaction.stage2.SecondUserService: transaction.stage2.SecondUserService.saveSecondTransactionWithNever is Actual Transaction Active : ❌ false
        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNever");
    }
}
