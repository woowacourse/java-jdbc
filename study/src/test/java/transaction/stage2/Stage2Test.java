package transaction.stage2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
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
     *
     * Propagation.REQUIRED
     * 메서드가 호출될 때 이미 진행 중인 트랜잭션이 있다면 그 트랜잭션을 사용하고,
     * 그렇지 않으면 새로운 트랜잭션을 시작
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
     * Propagation.REQUIRED_NEW
     * 메서드가 호출될 때 항상 새로운 트랜잭션을 시작
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
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는 지 확인해보자.
     */
    @Test
    void testRequiredNewWithRollback() {
        assertThat(firstUserService.findAll()).hasSize(0);

        assertThatThrownBy(() -> firstUserService.saveAndExceptionWithRequiredNew())
                .isInstanceOf(RuntimeException.class);

        // secondUserService에서 저장한 user만 존재
        assertThat(firstUserService.findAll()).hasSize(1)
                .extracting(User::getAccount)
                .containsExactly("gugu2");
    }

    /**
     * FirstUserService.saveFirstTransactionWithSupports() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     *
     * Propagation.SUPPORTS
     * 현재 트랜잭션이 활성화되어 있으면, 해당 트랜잭션에 참여
     */
    @Nested
    class SupportTest {

        @Test
        void saveFirstTransactionWithSupports() {
            final var actual = firstUserService.saveFirstTransactionWithSupports(false);

            log.info("transactions : {}", actual);
            assertThat(actual)
                    .hasSize(1)
                    .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithSupports");
        }

        @Test
        void saveFirstWithSupports() {
            final var actual = firstUserService.saveFirstWithSupports(false);

            log.info("transactions : {}", actual);
            assertThat(actual)
                    .hasSize(1)
                    .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithSupports");
        }

        @Test
        void saveFirstWithSupportsRollback() {
            // saveFirstWithSupports 테스트에서 트랜잭션이 반환되었지만, 롤백은 수행되지 않음
            assertThatThrownBy(() -> firstUserService.saveFirstWithSupports(true));
            assertThat(firstUserService.findAll())
                    .hasSize(2)
                    .extracting(User::getAccount)
                    .containsExactly("gugu", "gugu2");
        }

        @Test
        void saveFirstTransactionWithSupportsRollback() {
            assertThatThrownBy(() -> firstUserService.saveFirstTransactionWithSupports(true));
            assertThat(firstUserService.findAll()).hasSize(0);
        }
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     *
     * Propagation.MANDATORY
     * 활성화된 트랜잭션이 있는 경우 해당 트랜잭션에 참여
     * 트랜잭션이 없는 경우 예외가 발생한다.
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
     * Propagation.NOT_SUPPORTED
     * 비트랜잭셔널 상태로 실행, 즉 해당 메서드 내에서 데이터베이스 작업이 트랜잭션에 포함되지 않음
     */
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly(
                        "transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported",
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported"
                );
    }

    /**
     * 아래 테스트는 왜 실패할까?
     * 중첩된 트랜잭션은 JDBC 저장소를 사용할 떄 주로 savepoint를 기반으로 작동하며, JPA와의 호환성 이슈 발생
     * (JpaDialect dose not support savepoints)
     *
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     * 기존에 활성화된 트랜잭션이 존재하지 않아 NESTED가 REQUIRED처럼 동작
     *
     * Propagation.NESTED
     * 활성화된 외부 트랜잭션이 있을 때 그 안에 중첩된 트랜잭션을 생성
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
     * Propagation.NEVER
     * 트랜잭션이 없는 상태에서만 실행, 트랜잭션 내에서 호출되며 예외 발생
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
