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
     * - 생성된 트랜잭션은 1개
     * 왜 그런 결과가 나왔을까?
     * - 두번째 메서드의 트랜잭션 전파 레벨이 PROPAGATION_REQUIRED이기 때문
     * - 첫번째 트랜잭션 생성 후, 두번째 트랜잭션은 시작되지 않고 첫 트랜잭션에 참여함
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
     * - 생성된 트랜잭션은 2개
     * 왜 그런 결과가 나왔을까?
     * - 두번째 메서드의 트랜잭션 전파 레벨이 PROPAGATION_REQUIRED_NEW이기 때문
     * - 첫번째 트랜잭션이 있어도, 새로운 트랜잭션을 생성함
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
     * - 새 트랜잭션에서 생성된 save()는 예외가 터지지 않아 롤백되지 않음
     * - 기존 트랜잭션에서의 save()만 롤백이 된다
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
     * - 주석 있을 시 : 첫 트랜잭션만 실행됨
     * - 주석 없을 시 : 새 트랜잭션만 실행됨
     * Propagation.supports -> 기존 트랜잭션이 있으면 거기 붙는 놈
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
     * - 주석 있을 시 : 예외 발생
     * - 주석 없을 시 : 첫 트랜잭션만 실행 됨
     *  Propagation.Mandatory -> 말 그대로 기존 트랜잭션이 의무로 있어야됨
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
     * 주석 있을 시 : 트랜잭션 1개
     * 주석 없을 시 : 트랜잭션 2개
     * 실제 데이터베이스의 트랜잭션 -> 물리적 트랜잭션
     * 스프링이 처리하는 트랜잭션 -> 논리적 트랜잭션
     * https://docs.spring.io/spring-framework/docs/4.2.x/spring-framework-reference/html/transaction.html
     *
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
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     *
     * - 주석 있을 시: 두 번째 트랜잭션만 실행됨
     * - 주석 없을 시: 예외발생 NestedTransactionNotSupportedException -> JPA는 변경감지로 업데이트해서 못쓴다~
     * https://velog.io/@changyeonyoo/%EB%8F%84%EB%8C%80%EC%B2%B4-%EC%99%9C-%EB%A1%A4%EB%B0%B1%EB%90%98%EB%8A%94%EA%B1%B0%EC%95%BC
     * Propagation.Nested -> 새로 트랜잭션 만듬
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
     * - 주석 있을 시: 두 번째 트랜잭션만 실행됨 -> 스프링단에서만 논리적으로 실행되는 것뿐 물리적으로는 실행되지 않는듯..?
     * - 주석 없을 시: 예외발생 IllegalTransactionStateException -> never는 트랜잭션 못쓴다
     *
     * Propagation.Never -> 트랜잭션 멈춰!
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
