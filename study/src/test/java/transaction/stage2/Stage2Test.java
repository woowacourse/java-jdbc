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
     * 생성된 트랜잭션이 몇 개인가: 1개
     * 왜 그런 결과가 나왔을까: REQUIRES는 기존 Transaction이 존재하면 합류하고, 그렇지 않다면 새로 생성한다.
     */
    @Test
    void testRequired() {
        final var actual = firstUserService.saveFirstTransactionWithRequired();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .contains("transaction.stage2.FirstUserService.saveFirstTransactionWithRequired");
    }

    /**
     * 생성된 트랜잭션이 몇 개인가: 2개
     * 왜 그런 결과가 나왔을까: REQUIRES_NEW는 기존 Transaction이 존재하면 중지하고 새로운 Transaction을 생성한다.
     * 기존 REQUIRES (Transaction 생성) -> REQUIRES_NEW (새로운 Transaction 생성)
     */
    @Test
    void testRequiredNew() {
        final var actual = firstUserService.saveFirstTransactionWithRequiredNew();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactlyInAnyOrder( // return이 Set<>이네요 :sad:
                        "transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew",
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew"
                );
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는지 확인해보자.
     * 부모: REQUIRED (자식 메서드 호출, 커밋되지 않음)
     * 자식: REQUIRES_NEW (트랜잭션 새로 시작, 데이터 삽입, 커밋까지 이뤄짐)
     * REQUIRED에서 예외가 발생하더라도 커밋된 것이 롤백되지는 않는다 (다른 트랜잭션이므로)
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
     * Transactional이 존재하지 않음 -> save()에서 내부적으로 Transaction이 생성 및 커밋됨
     * 이후 자식 메서드의 SUPPORTS -> 존재하면 사용하고, 그렇지 않으면 non-transactional로 진행함
     * 자식 내부에 save()도 내부적으로 Transactional을 생성 및 커밋
     * TransactionManager에게는 SUPPORTS도 하나의 Transaction으로 보는 듯함. 다만 실제 Transaction이 유지되고 있지는 않은 것으로 보임.
     *
     * 주석을 해제하면 기존 Transaction을 이어받아 진행하게 됨. SUPPORTS는 기존에 존재한다면 REQUIRED처럼 합류한다.
     */
    @Test
    void testSupports() {
        final var actual = firstUserService.saveFirstTransactionWithSupports();

        log.info("transactions : {}", actual);

        // Without Transactional on saveFirstTransactionWithSupports
//        assertThat(actual)
//                .hasSize(1)
//                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithSupports");
//

        // With Transactional on saveFirstTransactionWithSupports
        assertThat(actual)
                .hasSize(2)
                .containsExactly(
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithSupports"
                );
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자. SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     *
     * MANDATORY 는 Transactional을 강제한다. Transaction이 존재하지 않으면 예외를 발생시키고, 존재하면 합류한다.
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
     */
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(0)
                .containsExactly("");
    }

    /**
     * 아래 테스트는 왜 실패할까?
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     */
    @Test
    void testNested() {
        final var actual = firstUserService.saveFirstTransactionWithNested();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(0)
                .containsExactly("");
    }

    /**
     * 마찬가지로 @Transactional을 주석처리하면서 관찰해보자.
     */
    @Test
    void testNever() {
        final var actual = firstUserService.saveFirstTransactionWithNever();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(0)
                .containsExactly("");
    }
}
