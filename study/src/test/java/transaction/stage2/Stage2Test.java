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
     * 왜 그런 결과가 나왔을까?
     *
     * 1개
     * REQUIRED 옵션에서는 기존의 트랜잭션에 참여한다.
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
     * 2개
     * REQUIRED_NEW 옵션에서는 독립된 트랜잭션을 새로 생성한다.
     */
    @Test
    void testRequiredNew() {
        final var actual = firstUserService.saveFirstTransactionWithRequiredNew();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly(
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew",
                        "transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew");
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는 지 확인해보자.
     *
     * firstUserService.saveAndExceptionWithRequiredNew가 첫 번째로 실행되는 트랜잭션이다.
     * secondUserService.saveSecondTransactionWithRequiresNew의 트랜잭션이 참여한다.
     * 트랜잭션 옵션이 REQUIRES_NEW이므로, 새로운 트랜잭션에서 사용자를 생성하고 저장한다.
     * secondUserService의 트랜잭션이 종료된 후 firstUserService에서 예외를 발생해 트랜잭션을 롤백한다.
     * 롤백하더라도 secondUserService의 트랜잭션과 별개이므로 user repository에 1개의 행이 존재한다.
     *
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
     *
     * 트랜잭션이 있으면 참여하고, 없으면 하지 않는다.
     * 즉 주석이 아닌 상태에서만 트랜잭션으로 동작하고, 주석 상태에서는 트랜잭션으로 동작하지 않는다.
     * 트랜잭션으로 동작하지 않더라도 transaction scope에 포함되기 때문에 로그로 출력됩니다.
     */
    @Test
    void testSupports() {
        final var actual = firstUserService.saveFirstTransactionWithSupports();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithSupports");   // 주석 아님
//                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithSupports");     // 주석
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     *
     * 기존의 트랜잭션이 없이 MANDATORY 옵션을 가진 트랜잭션 메서드가 실행될 경우 다음의 예외가 발생한다.
     * org.springframework.transaction.IllegalTransactionStateException: No existing transaction found for transaction marked with propagation 'mandatory'
     * SUPPORTS는 트랜잭션이 있으면 참여하고 없으면 트랜잭션 없이 실행된다는 차이가 있다.
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
     * 주석 처리 전 1개
     * - 로그에는 2개가 기록되지만, 트랜잭션에서 로직이 실행되지는 않습니다.
     * 주석 처리 후 0개
     * - 로그에는 1개가 기록되지만, 트랜잭션에서 로직이 실행되지는 않습니다.
     * <p>
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     *
     * 물리적 트랜잭션은 DB가 관리하는 트랜잭션 단위를 의미한다.
     * MySQL에서는 'BEGIN TRANSACTION` 또는 'START TRANSACTION;' 명령어로 시작된다.
     * 논리적 트랜잭션은 프로그래밍 언어 또는 프레임워크에서 제공하는 트랜잭션 단위를 의미한다.
     * 스프링의 @Transactional 애너테이션으로 시작되는 트랜잭션이 여기에 해당한다.
     */
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
//                .hasSize(2)     // 주석 전
//                .containsExactly(
//                        "transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported",
//                        "transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported");     // 주석 전
                .hasSize(1)     // 주석 후
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported");     // 주석 후
    }

    /**
     * 아래 테스트는 왜 실패할까?
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     *
     * NESTED는 트랜잭션이 있을 경우 savepoint로 트랜잭션에 참여합니다.
     * 트랜잭션이 없는 경우 REQUIRED와 마찬가지로 트랜잭션을 시작합니다.
     * 그런데 H2는 savepoint를 지원하지 않아서 예외가 발생합니다.
     *
     * 결과적으로 @Transactional을 주석 처리하면 secondUserService.saveSecondTransactionWithNested() 메서드만 트랜잭션에서 실행됩니다.
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
     *
     * NEVER는 기존 트랜잭션이 있는 경우 예외를 발생시킵니다.
     * org.springframework.transaction.IllegalTransactionStateException: Existing transaction found for transaction marked with propagation 'never'
     *
     * 주석처리하면 트랜잭션 없이 정상 동작합니다.
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
