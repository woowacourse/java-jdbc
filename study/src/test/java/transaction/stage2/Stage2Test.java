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
     * 생성된 트랜잭션이 몇 개인가? 1개
     * 왜 그런 결과가 나왔을까?
     * <p>
     * REQUIRED: defualt 설정: 활성화된 트랜잭션이 있는지 체크하고, 없다면 새로운 트랜잭션을 생성
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
     * 생성된 트랜잭션이 몇 개인가? 2개
     * 왜 그런 결과가 나왔을까?
     * <p>
     * REQUIRED_NEW: 현재 진행 중인 트랜잭션이 있더라도 무조건 새로운 트랜잭션을 생성한다. 새로 시작된 트랜잭션은 이전 트랜잭션과 완전히 독립적이다.
     */
    @Test
    void testRequiresNew() {
        final var actual = firstUserService.saveFirstTransactionWithRequiresNew();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithRequiresNew",
                        "transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew");
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는 지 확인해보자.
     * <p>
     * REQUIRES_NEW는 이미 존재하는 트랜잭션과 독립적으로 동작하는 새로운 트랜잭션을 생성하므로, 영향을 받지 않는다.
     */
    @Test
    void testRequiresNewWithRollback() {
        assertThat(firstUserService.findAll()).hasSize(0);

        assertThatThrownBy(() -> firstUserService.saveAndExceptionWithRequiresNew())
                .isInstanceOf(RuntimeException.class);

        assertThat(firstUserService.findAll()).hasSize(1);
    }

    /**
     * FirstUserService.saveFirstTransactionWithSupports() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * <p>
     * SUPPORTS: 스프링은 우선 활성화된 트랜잭션이 존재하는지 체크한다. 만약, 존재한다면, 해당 트랜잭션이 사용된다. 그렇지 않다면, non-transactional로 수행된다.
     * <p>
     * 따라서, 주석 처리 되어있을 때에는 트랜잭션 없이 동작한다.
     * 주석 처리를 해제하면 FirstUserService의 트랜잭션에 참여한다.
     */
    @Test
    void testSupports() {
        final var actual = firstUserService.saveFirstTransactionWithSupports();

        log.info("transactions : {}", actual);
        /**
         * 주석 처리를 하지 않은 상태에서는 활성화되어있는 FirstTransaction을 사용한다.
         * 그렇지 않을 때에는, 물리적으로 활성화된 트랜잭션 없이 동작한다.
         */
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithSupports");
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     * <p>
     * MANDATORY: 만약 활성화된 트랜잭션이 있다면, 해당 트랜잭션을 사용한다. 그렇지 않다면, 스프링은 예외를 발생시킨다.
     */
    @Test
    void testMandatory() {
        final var actual = firstUserService.saveFirstTransactionWithMandatory();

        log.info("transactions : {}", actual);
        /**
         * 주석 처리가 된 상태에서는 활성화된 트랜잭션이 없으므로, 예외가 발생한다.
         *
         * 주석 처리를 해제하면, 이미 할성화된 firstTransactionWithMandatory의 트랜잭션을 사용한다.
         */
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithMandatory");
    }

    /**
     * 아래 테스트는 몇 개의 물리적 트랜잭션이 동작할까? 1개!
     * FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석 처리하자.
     * <p>
     * NOT_SUPPORTED: 만약 현재 트랜잭션이 존재한다면, 스프링은 우선 트랜잭션을 일시 정지한 후 트랜잭션 없이 비즈니스 로직을 수행한다.
     * <p>
     * 다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이 동작할까? 0개!
     * <p>
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     * <p>
     * 스프링에서는 논리 트랜잭션이라는 추가적인 메커니즘을 통해 트랜잭션을 다룬다. 실제 데이터베이스의 트랜잭션과 상응하는 개념을 물리 트랜잭션이라고 한다.
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
     * <p>
     * NESTED: 기존 트랜잭션에 중첩된다. 부모 트랜잭션이 존재할 경우, 새로운 트랜잭션이 부모 트랜잭션의 하위로 중첩된다.
     * 스프링은 트랜잭션이 존재하는지 체크하고, 존재한다면 save point를 표시한다. 이것은 우리 비즈니스 로직 수행이 예외를 던진다면, 해당 지점까지 롤백을 하겠다는 것을 의미한다.
     * 만약, 다른 활성화된 트랜잭션이 없다면 REQUIRED 옵션처럼 동작한다.
     */
    @Test
    void testNested() {
        final var actual = firstUserService.saveFirstTransactionWithNested();

        log.info("transactions : {}", actual);
        /**
         * 주석 없을 때에는 Hibernate 구현체가 지원하지 않는다고 예외를 발생시킨다.
         * 주석처리가된 상태에서는 SecondTransaction의 트랜잭션이 활성화된다.
         */
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNested");
    }

    /**
     * 마찬가지로 @Transactional을 주석처리하면서 관찰해보자.
     * <p>
     * NEVER: 활성화된 트랜잭션이 있다면 예외를 발생시킨다.
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
