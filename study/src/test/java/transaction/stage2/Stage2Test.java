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
     * 생성된 트랜잭션이 몇 개인가?
     * 왜 그런 결과가 나왔을까?
     */
    // REQUIRED는 활성화된 트랜잭션이 없다면, 새로운 트랜잭션을 생성한다.
    // 이미 활성화된 트랜잭션이 있으면, 그 트랜잭션을 그대로 사용한다.
    @Test
    void testRequired() {
        final var actual = firstUserService.saveFirstTransactionWithRequired();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithRequired");
    }

    /**
     * 생성된 트랜잭션이 몇 개인가?
     * 왜 그런 결과가 나왔을까?
     */
    // REQUIRED_NEW는 항상 새로운 트랜잭션을 시작한다.
    // 새로운 트랜잭션은 호출한 메서드의 트랜잭션과는 독립적으로 작동하기 때문에,
    // 내부 메서드에서 트랜잭션이 실패하더라도, 외부 트랜잭션에는 영향을 주지 않는다.
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
     */
    // REQUIRED_NEW는 독립된 트랜잭션이므로, 롤백되더라도 외부 트랜잭션의 데이터는 유지된다.
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
     */
    // SUPPORTS는 트랜잭션이 있으면 그 트랜잭션 안에서 실행된다.
    // 트랜잭션이 없으면 그냥 비트랜잭션으로 실행된다.
    // 주석 O (트랜잭션 X) -> 1 / "transaction.stage2.SecondUserService.saveSecondTransactionWithSupports"
    // 주석 X (트랜잭션 O) -> 1 / "transaction.stage2.FirstUserService.saveFirstTransactionWithSupports"
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
     */
    // MANDATORY는 반드시 현재 활성화된 트랜잭션이 존재해야만 메서드가 실행될 수 있다.
    // 주석 O (트랜잭션 X) -> No existing transaction found for transaction marked with propagation 'mandatory'
    // 주석 X (트랜잭션 O) -> 1 / "transaction.stage2.FirstUserService.saveFirstTransactionWithMandatory"
    @Test
    void testMandatory() {
        final var actual = firstUserService.saveFirstTransactionWithMandatory();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithMandatory");
    }

    /**
     * 아래 테스트는 몇 개의 물리적 트랜잭션이 동작할까?
     * FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석 처리하자.
     * 다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이 동작할까?
     *
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     */
    // 물리적 트랜잭션: 데이터베이스가 실제로 관리하는 트랜잭션
    // 논리적 트랜잭션: 프레임워크 수준에서 관리되는 트랜잭션
    // NOT_SUPPORTED는 활성화된 트랜잭션이 있으면 이를 일시 중단하고, 비트랜잭션 모드로 메서드를 실행한다.
    // 트랜잭션이 없으면, 비트랜잭션 모드로 실행된다.
    // 주석 X (트랜잭션 O) -> 2 / "transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported"
    //                          "transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported"
    //                  -> transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported is Actual Transaction Active : ✅ true
    //                  -> transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported is Actual Transaction Active : ❌ false
    // 주석 O (트랜잭션 X) -> 1 / "transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported"
    //                  -> null is Actual Transaction Active : ❌ false
    //                  -> transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported is Actual Transaction Active : ❌ false
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported");
    }

    /**
     * 아래 테스트는 왜 실패할까?
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     */
    // NESTED는 상위 트랜잭션이 이미 존재하는 경우, 중첩된 트랜잭션이 생성된다.
    // 상위 트랜잭션이 커밋되면, 중첩 트랜잭션도 함께 커밋된다.
    // 중첩 트랜잭션이 커밋되더라도, 상위 트랜잭션이 롤백되면 중첩 트랜잭션도 롤백된다.
    // 중첩 트랜잭션 내에서 예외가 발생하고 롤백되더라도, 상위 트랜잭션은 롤백되지 않는다.
    // 상위 트랜잭션이 없으면 REQUIRED와 동일하게 새로운 트랜잭션을 생성한다.
    // 주석 X (트랜잭션 O) -> JpaDialect does not support savepoints - check your JPA provider's capabilities
    //                  -> 중첩 트랜잭션을 사용하려고 했으나, JPA 구현체가 이를 지원하지 않아서 테스트가 실패한다.
    // 주석 O (트랜잭션 X) -> 1 / "transaction.stage2.SecondUserService.saveSecondTransactionWithNested"
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
     */
    // NEVER는 트랜잭션이 있으면 예외를 발생시킨다.
    // 트랜잭션이 없으면, 비트랜잭션 모드로 실행된다.
    // 주석 X (트랜잭션 O) -> Existing transaction found for transaction marked with propagation 'never'
    // 주석 O (트랜잭션 X) -> 1 / "transaction.stage2.SecondUserService.saveSecondTransactionWithNever"
    @Test
    void testNever() {
        final var actual = firstUserService.saveFirstTransactionWithNever();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNever");
    }
}
