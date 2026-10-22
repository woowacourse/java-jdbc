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
     *
     * 1개. SecondUserService도 REQUIRED라서 새 트랜잭션을 만들지 않고
     * FirstUserService에서 시작한 트랜잭션에 참여한다.
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
     * 2개. REQUIRES_NEW는 기존 트랜잭션을 잠시 보류(suspend)하고 항상 새로운 물리적 트랜잭션을 시작한다.
     */
    @Test
    void testRequiredNew() {
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
        assertThat(firstUserService.findAll()).hasSize(0);

        assertThatThrownBy(() -> firstUserService.saveAndExceptionWithRequiredNew())
                .isInstanceOf(RuntimeException.class);

        // REQUIRES_NEW로 저장한 데이터는 별도 트랜잭션으로 이미 커밋되어 롤백되지 않는다.
        // FirstUserService에서 저장한 데이터만 롤백된다.
        assertThat(firstUserService.findAll()).hasSize(1);
    }

    /**
     * FirstUserService.saveFirstTransactionWithSupports() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     *
     * 주석 처리 : 진행 중인 트랜잭션이 없으므로 SUPPORTS는 트랜잭션 없이 실행된다.
     *            이름은 saveSecondTransactionWithSupports로 잡히지만 실제 트랜잭션은 비활성(❌)이다.
     * 주석 해제 : FirstUserService의 트랜잭션에 참여하므로 1개의 트랜잭션만 존재한다.
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
     *
     * 주석 처리 : 진행 중인 트랜잭션이 없으므로 IllegalTransactionStateException이 발생한다.
     * 주석 해제 : FirstUserService의 트랜잭션에 참여하므로 1개의 트랜잭션만 존재한다.
     * SUPPORTS는 트랜잭션이 없으면 트랜잭션 없이 실행하지만, MANDATORY는 트랜잭션이 없으면 예외를 던진다.
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
     * 주석 해제 : 물리적 트랜잭션 2개.
     *            NOT_SUPPORTED는 FirstUserService의 트랜잭션을 보류(suspend)하고 트랜잭션 없이 실행된다. (❌)
     *            그래서 내부의 userRepository.save()가 자신의 @Transactional로 새 물리적 트랜잭션을 만든다.
     * 주석 처리 : 물리적 트랜잭션 2개.
     *            두 서비스 모두 트랜잭션 없이 실행되고, 각 userRepository.save()가 따로 물리적 트랜잭션을 만든다.
     *
     * 물리적 트랜잭션 : 실제 DB 커넥션으로 시작·커밋·롤백되는 트랜잭션
     * 논리적 트랜잭션 : 스프링이 @Transactional 메서드마다 관리하는 트랜잭션 범위. 여러 논리적 트랜잭션이 하나의 물리적 트랜잭션에 참여할 수 있다.
     */
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported",
                        "transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported"
                );
    }

    /**
     * 아래 테스트는 왜 실패할까?
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     *
     * NESTED는 세이브포인트를 사용하는데 JpaTransactionManager(HibernateJpaDialect)는 세이브포인트를 지원하지 않아
     * NestedTransactionNotSupportedException이 발생한다.
     * 주석 처리하면 진행 중인 트랜잭션이 없으므로 REQUIRED처럼 새로운 트랜잭션을 시작한다.
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
     * 주석 해제 : 진행 중인 트랜잭션이 있으므로 IllegalTransactionStateException이 발생한다.
     * 주석 처리 : 진행 중인 트랜잭션이 없으므로 트랜잭션 없이 실행된다.
     *            이름은 saveSecondTransactionWithNever로 잡히지만 실제 트랜잭션은 비활성(❌)이다.
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
