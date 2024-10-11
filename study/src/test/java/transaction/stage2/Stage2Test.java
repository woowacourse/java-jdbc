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
     * 생성된 트랜잭션이 몇 개인가? => 1개
     * 왜 그런 결과가 나왔을까?
     * REQUIRED = 이미 진행 중인 트랜잭션이 있으면 그 트랜잭션에 참여하고 없으면 새로운 트랜잭션을 생성한다.
     * 첫 번째 메서드에서 트랜잭션을 생성하고 그 이후 호출된 메서드는 같은 트랜잭션에 참여했기 때문에 트랜잭션이 1개만 생성된다.
     * firstTransactionName is Actual Transaction Active : ✅ true
     * secondTransactionName is Actual Transaction Active : ✅ true
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
     * 생성된 트랜잭션이 몇 개인가? => 2개
     * 왜 그런 결과가 나왔을까?
     * REQUIRES_NEW = 기존에 진행 중인 트랜잭션이 있어도 새로운 트랜잭션을 무조건 생성한다.
     * FirstUserService.saveFirstTransactionWithRequiredNew()에서 SecondUserService.saveSecondTransactionWithRequiresNew()를 호출하면
     * 새로운 트랜잭션이 생성되어 총 2개의 트랜잭션이 발생한다.
     * firstTransactionName is Actual Transaction Active : ✅ true
     * secondTransactionName is Actual Transaction Active : ✅ true
     */
    @Test
    void testRequiredNew() {
        final var actual = firstUserService.saveFirstTransactionWithRequiredNew();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew",
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew");
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는 지 확인해보자.
     * 예외가 발생한 REQUIRES_NEW 트랜잭션은 롤백되고 saveFirstTransactionWithRequiredNew 는 정상 커밋된다.
     * secondTransactionName is Actual Transaction Active : ✅ true
     * firstTransactionName is Actual Transaction Active : ❌ false
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
     * 주석 처리: 트랜잭션 없이 메서드가 실행되므로 트랜잭션이 생성되지 않는다.
     * SUPPORTS = 트랜잭션이 있을 때 트랜잭션에 참여하고 없으면 트랜잭션 없이 실행된다.
     * firstTransactionName is Actual Transaction Active : ✅ true
     * secondTransactionName is Actual Transaction Active : ❌ false
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
     * MANDATORY = 반드시 기존 트랜잭션이 있어야 한다.
     * SUPPORTS는 트랜잭션이 없으면 트랜잭션 없이 실행되지만 MANDATORY는 트랜잭션이 없으면 예외를 던진다.
     * firstTransactionName is Actual Transaction Active : ✅ true
     * secondTransactionName is Actual Transaction Active : ✅ true
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
     * 아래 테스트는 몇 개의 물리적 트랜잭션이 동작할까? => 1
     * FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석 처리하자.
     * 다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이 동작할까? => 0
     * <p>
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     * 물리 트랜잭션: 실제 데이터베이스와 연결되어 있는 트랜잭션
     * 논리 트랜잭션: 물리적 트랜잭션 위에서 실행되는 스프링의 트랜잭션 관리 단위 - 하나의 물리적 트랜잭션 내에 여러 논리적 트랜잭션이 포함될 수 있다.
     * firstTransactionName is Actual Transaction Active : ✅ true
     * secondTransactionName is Actual Transaction Active : ❌ false
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
     * 아래 테스트는 왜 실패할까?
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     * NESTED = 기존 트랜잭션 내에서 별도의 중첩 트랜잭션을 생성한다.
     * @Transactional이 없으면 중첩 트랜잭션이 생성되지 않으므로 트랜잭션 없이 메서드가 실행된다.
     * firstTransactionName is Actual Transaction Active : ✅ true
     * secondTransactionName is Actual Transaction Active : ✅ true
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
     * NEVER = 트랜잭션이 있으면 예외 발생
     * firstTransactionName is Actual Transaction Active : ❌ false
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
