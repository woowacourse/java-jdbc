package transaction.stage2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.NestedTransactionNotSupportedException;
import org.springframework.transaction.support.TransactionTemplate;

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
    private SecondUserService secondUserService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

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
        assertThat(userRepository.count()).isZero();
        final var actual = firstUserService.saveFirstTransactionWithRequiredNew();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew",
                        "transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew");
        assertThat(userRepository.count()).isEqualTo(2);
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는 지 확인해보자.
     */
    @Test
    void testRequiredNewWithRollback() {
        assertThat(firstUserService.findAll()).isEmpty();

        assertThatThrownBy(() -> firstUserService.saveAndExceptionWithRequiredNew())
                .isInstanceOf(RuntimeException.class);

        // 외부 트랜잭션은 롤백되지만, REQUIRES_NEW로 먼저 커밋한 내부 저장은 남는다.
        assertThat(firstUserService.findAll()).hasSize(1);
    }

    /**
     * FirstUserService.saveFirstTransactionWithSupports() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     */
    @Test
    void testSupports() {
        assertThat(userRepository.count()).isZero();
        final var actual = firstUserService.saveFirstTransactionWithSupports();

        log.info("transactions : {}", actual);
        assertThat(actual)
                // 이름이 있어도 실제 트랜잭션이 활성 상태라는 뜻은 아니다.
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithSupports");
        // 두 save()가 각각 트랜잭션을 시작하고 커밋한다.
        assertThat(userRepository.count()).isEqualTo(2);
    }

    @Test
    void testSupportsWithExistingTransaction() {
        assertThat(userRepository.count()).isZero();
        final var transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setName("supports-learning-outer");

        transactionTemplate.executeWithoutResult(status -> {
            final var actual = firstUserService.saveFirstTransactionWithSupports();

            assertThat(actual).containsExactly("supports-learning-outer");
            assertThat(userRepository.count()).isEqualTo(2);
            // 외부 트랜잭션에 참여한 두 저장이 함께 취소되는지 확인한다.
            status.setRollbackOnly();
        });

        assertThat(userRepository.count()).isZero();
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     */
    @Test
    void testMandatory() {
        assertThat(userRepository.count()).isZero();

        assertThatThrownBy(() -> firstUserService.saveFirstTransactionWithMandatory())
                .isInstanceOf(IllegalTransactionStateException.class);

        // 첫 번째 save()는 이미 커밋됐고, MANDATORY인 두 번째 서비스는 실행되지 않는다.
        assertThat(userRepository.count()).isEqualTo(1);
    }

    @Test
    void testMandatoryWithExistingTransaction() {
        assertThat(userRepository.count()).isZero();
        final var transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setName("mandatory-learning-outer");

        transactionTemplate.executeWithoutResult(status -> {
            final var actual = firstUserService.saveFirstTransactionWithMandatory();

            assertThat(actual).containsExactly("mandatory-learning-outer");
            assertThat(userRepository.count()).isEqualTo(2);
            // 두 저장이 기존 트랜잭션에 참여하므로 외부 롤백으로 함께 취소된다.
            status.setRollbackOnly();
        });

        assertThat(userRepository.count()).isZero();
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
        assertThat(userRepository.count()).isZero();
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
                // NOT_SUPPORTED 범위의 이름은 실제 DB 트랜잭션의 존재를 뜻하지 않는다.
                .containsExactlyInAnyOrder(
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported",
                        "transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported");
        assertThat(userRepository.count()).isEqualTo(2);
    }

    @Test
    void testNotSupportedWithOuterRollback() {
        assertThat(userRepository.count()).isZero();
        final var transactionTemplate = new TransactionTemplate(transactionManager);
        transactionTemplate.setName("not-supported-learning-outer");

        transactionTemplate.executeWithoutResult(status -> {
            final var actual = firstUserService.saveFirstTransactionWithNotSupported();

            assertThat(actual).containsExactlyInAnyOrder(
                    "not-supported-learning-outer",
                    "transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported");
            status.setRollbackOnly();
        });

        // 외부 트랜잭션을 보류한 동안 Repository.save()가 별도로 커밋한 저장은 남는다.
        assertThat(userRepository.count()).isEqualTo(1);
    }

    /**
     * 아래 테스트는 왜 실패할까?
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     */
    @Test
    void testNested() {
        assertThat(userRepository.count()).isZero();

        assertThatThrownBy(() -> firstUserService.saveFirstTransactionWithNested())
                .isInstanceOf(NestedTransactionNotSupportedException.class);

        // 현재 JPA 구성에서는 저장점 생성에 실패하고 외부 저장도 롤백된다.
        assertThat(userRepository.count()).isZero();
    }

    @Test
    void testNestedWithoutExistingTransaction() {
        assertThat(userRepository.count()).isZero();

        final var actual = secondUserService.saveSecondTransactionWithNested();

        // 외부 트랜잭션이 없으면 REQUIRED처럼 새 트랜잭션으로 실행된다.
        assertThat(actual).isEqualTo("transaction.stage2.SecondUserService.saveSecondTransactionWithNested");
        assertThat(userRepository.count()).isEqualTo(1);
    }

    /**
     * 마찬가지로 @Transactional을 주석처리하면서 관찰해보자.
     */
    @Test
    void testNever() {
        assertThat(userRepository.count()).isZero();

        assertThatThrownBy(() -> firstUserService.saveFirstTransactionWithNever())
                .isInstanceOf(IllegalTransactionStateException.class);

        // NEVER의 예외가 외부 REQUIRED까지 전파되어 첫 번째 저장도 롤백된다.
        assertThat(userRepository.count()).isZero();
    }

    @Test
    void testNeverWithoutExistingTransaction() {
        assertThat(userRepository.count()).isZero();

        // REQUIRED인 첫 번째 서비스를 거치지 않고 스프링 빈을 직접 호출한다.
        secondUserService.saveSecondTransactionWithNever();

        // NEVER는 진입 시 트랜잭션을 거부하지만, 내부 save()의 트랜잭션을 금지하지 않는다.
        assertThat(userRepository.count()).isEqualTo(1);
    }
}
