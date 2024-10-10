package transaction.stage2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.NestedTransactionNotSupportedException;

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
     *
     * 부모 트랜잭션이 있으면 그대로 사용하므로 1개
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
     * 생성된 트랜잭션이 몇 개인가?
     * 왜 그런 결과가 나왔을까?
     *
     * 부모 트랜잭션과 별개의 트랙잭션을 새로 생성하므로 2개
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
     *
     * 자식 트랜잭션은 부모와 별개인 물리 트랜잭션을 사용하므로 부모에서 예외가 발생해도 자식을 커밋됨
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
     *
     * SUPPORTS 는 부모 트랜잭션이 있다면 합류하고, 없다면 작동하지 않는 트랜잭션이 명시적으로만 생성된다.
     */
    @Test
    void testSupports() {
        final var actual = firstUserService.saveFirstTransactionWithSupports();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithSupports");
        // 로그 보면 부모 트랜잭션 없을 때 둘 다 Actual Transaction Active : ❌ false 로 나옴
        // 부모 트랜잭션이 있다면 둘 다 Actual Transaction Active : ✅ true 로 나옴
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     *
     * MANDATORY 는 부모 트랜잭션이 있으면 합류하고, 없다면 예외를 발생시킨다.
     * (No existing transaction found for transaction marked with propagation 'mandatory')
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
     * 아래 테스트는 몇 개의 물리적 트랜잭션이 동작할까?
     * FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석 처리하자.
     * 다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이 동작할까?
     *
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     *
     * NOT_SUPPORTED에서는 부모 트랜잭션 유무에 상관없이 명시적으로 트랜잭션이 생성되지만 트랜잭션이 동작하지 않는다.
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
     *
     * NESTED는 부모 트랜잭션이 있으면 새로운 트랜잭션을 생성. REQUIRED_NEW와는 다르다.
     * 부모 트랜잭션의 커밋과 롤백에는 영향을 받지만 자신의 커밋과 롤백은 부모 트랜잭션에게 영향을 주지 않는다.
     * 즉, 자식 트랜잭션이 실패해도 부모 트랜잭션은 Rollback 되지 않지만,
     * 부모 트랜잭션이 실패하면 자식 트랜잭션은 Rollback 된다.
     *
     * NESTED에서, 중첩된 트랜잭션 내부에서 롤백 발생시 해당 중첩 트랜잭션의 시작 지점 까지만 롤백된다.
     * 이를 위해 스프링은 트랜잭션이 있는지 체크하고, 만일 있다면 새로운 save point로 표시한다.
     * 비즈니스 로직이 에러를 발생시키면 트랜잭션은 저장 포인트로 롤백된다.
     * JDBC 3.0 이후 지원되는 save point기능을 활용한다.
     * JPA를 사용하는 경우, 변경감지를 통해서 업데이트 쿼리를 최대한 지연해서 수행하는 방식을 사용하기 때문에 중첩된 트랜잭션 경계를 설정할 수 없어 지원하지 않는다.
     */
    @Test
    void testNested() {
//        final var actual = firstUserService.saveFirstTransactionWithNested();
//
//        log.info("transactions : {}", actual);
//        assertThat(actual)
//                .hasSize(0)
//                .containsExactly("");
        assertThatThrownBy(() ->firstUserService.saveFirstTransactionWithNested())
                .isInstanceOf(NestedTransactionNotSupportedException.class);
    }

    /**
     * 마찬가지로 @Transactional을 주석처리하면서 관찰해보자.
     *
     * NEVER은 트랜잭션이 명시적으로 표현은 되지만 트랜잭션을 아예 쓰지 않는 설정임.
     * 부모 트랜잭션이 존재할 경우 예외 발생
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
