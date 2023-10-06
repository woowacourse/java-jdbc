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
    @Test
    void testRequired() {
        // required: 트랜잭션 필요 -> 있으면 사용, 없으면 새로 생성
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
    @Test
    void testRequiredNew() {
        // REQUIRES_NEW: 이미 트랜잭션이 있어도 새로운 트랜잭션을 생성한다.
        final var actual = firstUserService.saveFirstTransactionWithRequiredNew();

        log.info("transactions : {}", actual);
        assertThat(actual)
            .hasSize(2)
            .containsExactlyInAnyOrder(
                "transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew",
                "transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew"
            );
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는 지 확인해보자.
     */
    @Test
    void testRequiredNewWithRollback() {
        // SecondService는 다른 트랜잭션을 가진다 -> rollback 되지 않음 => 여기서 save한 user는 commit 된다.
        // FirstSerivce는 예외 발생으로 rollback => 여기서 save 한 user는 rollback
        assertThat(firstUserService.findAll()).hasSize(0);

        assertThatThrownBy(() -> firstUserService.saveAndExceptionWithRequiredNew())
            .isInstanceOf(RuntimeException.class);

        assertThat(firstUserService.findAll()).hasSize(1);
    }

    /**
     * FirstUserService.saveFirstTransactionWithSupports() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     */
    @Test
    void testSupports() {
        // SUPPORTS: 부모 트랜잭션이 있으면 해당 트랜잭션을 사용, 없다면 트랜잭션이 없는 상태로 실행
        // 주석이라면? firstUserService -> 트랜잭션 없음. secondUserSerivce -> 트랜잭션 어노테이션 => Second만 논리적 트랜잭션, 물리적으로는 둘 다 없음
        // 주석이 아니라면? firstUserService, secondUserService 가 같은 트랜잭션 사용
        final var actual = firstUserService.saveFirstTransactionWithSupports();

        log.info("transactions : {}", actual);
        assertThat(actual)
            .hasSize(1)
            .containsExactlyInAnyOrder(
                "transaction.stage2.FirstUserService.saveFirstTransactionWithSupports"
            );
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     */
    @Test
    void testMandatory() {
        // MANDATORY: 트랜잭션이 있어야 함을 강제한다. 부모 트랜잭션이 있다면 사용하고, 없다면 IlleagalTransactionStateException
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
    @Test
    void testNotSupported() {
        // NOT_SUPPORTED: 기존 트랜잭션이 있든없든 트랜잭션을 실행하지 않는다. 기존 트랜잭션이 있었다면? 이를 보류
        // 주석있다면 1개 (SecondService 에 논리적, 물리적으로는 둘 다 X)
        // 주석없다면 2개 (FirstService: 논리적, 물리적 실행 / SecondService: 논리적O, 물리적 X)
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
    @Test
    void testNested() {
        // NESTED: 기존 트랜잭션이 있다면 중첩 트랜잭션 생성, 기존 트랜잭션이 없다면 새 트랜잭션 생성
        // 부모 트랜잭션에 문제가 생기면 중첩 트랜잭션은 영향을 받는다. 하지만 중첩 트랜잭션의 영향을 부모 트랜잭션이 받지는 않는다.
        // JPA 에서는 중첩으로 사용 불가 (즉 부모 트랜잭션이 있는 상황에서 NESTED 불가)
        // 주석 처리하면: 1개 (새로 생성), 외부에 영향 X
        // 주석 없으면: 2개, 외부1, 중첩1
        final var actual = firstUserService.saveFirstTransactionWithNested();

        log.info("transactions : {}", actual);
        assertThat(actual)
            .hasSize(1)
            .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNested");
    }

    /**
     * 마찬가지로 @Transactional을 주석처리하면서 관찰해보자.
     */
    @Test
    void testNever() {
        // NEVER: 트랜잭션 없이 실행. 부모 트랜잭션이 있었다면 IlleaglTransactionStateException 발생
        // firstService 에 @Transactional 있다면 예외 발생
        // @Transactional 주석이라면 실행 (트랜잭션 없이) -> 물리적 트랜잭션은 없지만, 논리적으로는 어노테이션이 있기 때문에 1개 반환?
        final var actual = firstUserService.saveFirstTransactionWithNever();

        log.info("transactions : {}", actual);
        assertThat(actual)
            .hasSize(1)
            .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNever");
    }
}
