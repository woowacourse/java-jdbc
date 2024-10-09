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
     * 생성된 트랜잭션이 몇 개인가? 1개
     * 왜 그런 결과가 나왔을까? Propagation.REQUIRED는 이미 진행 중인 트랜잭션이 있으면 그 트랜잭션을 사용하고 없으면 새로운 트랜잭션을 생성한다.
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
     * 왜 그런 결과가 나왔을까? Propagation.REQUIRES_NEW는 무조건 새로운 트랜잭션을 생성한다.
     */
    @Test
    void testRequiredNew() {
        final var actual = firstUserService.saveFirstTransactionWithRequiredNew();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly(
                        "transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew",
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew");
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는 지 확인해보자.
     * -> 내부에서 REQUIRES_NEW로 생성된 트랜잭션은 commit되어 user가 저장된다.
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
     * <p>
     * -> Propagation.SUPPORTS는 이미 진행 중인 트랜잭션이 있으면 그 트랜잭션을 사용하고 없으면 트랜잭션을 사용하지 않는다.
     * 주석이 없는 경우 트랜잭션을 사용하지 않는다. REQUIRED 주석을 사용하면 그 트랜잭션을 사용한다.
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
     * <p>
     * -> Propagation.MANDATORY는 이미 진행 중인 트랜잭션이 있으면 그 트랜잭션을 사용하고 없으면 예외를 발생시킨다.
     * SUPPORTS와 다른 점은 이미 진행 중인 트랜잭션이 없을 때 예외를 발생시킨다는 것이다.
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
     * 아래 테스트는 몇 개의 물리적 트랜잭션이 동작할까? 4개 (first, save, second, save)
     * FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석 처리하자.
     * 다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이 동작할까? 2개 (save, save)
     * <p>
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     * <p>
     * -> NOT_SUPPORTED는 이미 진행 중인 트랜잭션이 있으면 그 트랜잭션을 일시 중지하고 비트랜잭션 상태로 실행한다.
     * <p>
     * 물리적 트랜잭션은 데이터베이스에서 제공하는 트랜잭션을 말하고 논리적 트랜잭션은 개발자가 정의한 트랜잭션을 말한다.
     * 논리적 트랜잭션은 여러 물리적 트랜잭션을 하나의 단위로 묶어서 처리할 수 있다.
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
     * JPA에서 지원하지 않는 Propagation.NESTED 때문에 실패한다.
     * "JpaDialect does not support savepoints - check your JPA provider's capabilities"라는 에러 메시지가 나온다.
     * 변경감지를 통해서 업데이트문을 최대한 지연해서 발행하는 방식을 사용하기 때문에 중첩된 트랜잭션 경계를 설정할 수 없어 지원하지 않습니다.
     *
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     * NESTED가 REQUIRED와 같이 사용된다.
     *
     * -> NESTED는 현재 트랜잭션이 존재하는 경우, 새로운 중첩 트랜잭션을 생성합니다.
     * 이 중첩 트랜잭션은 부모 트랜잭션과 연결되어 있으며, 부모 트랜잭션이 커밋되면 중첩 트랜잭션도 커밋됩니다.
     * 만약 중첩 트랜잭션 내에서 예외가 발생하면, 해당 중첩 트랜잭션만 롤백되고 부모 트랜잭션은 계속 유지됩니다.
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
     * -> NEVER는 이미 진행 중인 트랜잭션이 있으면 예외를 발생시킨다.
     * 주석 처리하면 예외가 발생하지 않는다.
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
