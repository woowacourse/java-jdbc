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
     * 생성된 트랜잭션이 몇 개인가?
     * 왜 그런 결과가 나왔을까?
     * REQUIRED 속성의 경우 트랜잭션이 존재하면 해당 트랜잭션에 참여하기 때문에 생성된 트랜잭션은 1개이다.
     * 먼저 생성된 FirstUserService의 트랜잭션에 SecondUserService의 트랜잭션이 참여한다.
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
     * REQUIRED_NEW 속성의 경우 트랜잭션을 항상 새롭게 생성한다.
     * 먼저 생성된 FirstUserService의 트랜잭션과는 별개의 SecondUserService의 트랜잭션이 생성된다.
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
     * firstUserService와는 별개의 secondUserService의 트랜잭션이 생성되었다.
     * 이로인해 first의 롤백과는 별개로 secondUserService의 트랜잭션은 커밋되었으므로 최종적으로 1건이 저장된다.
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
     * Propagation Support 속성의 경우 트랜잭션이 있다면 참여, 없으면 트랜잭션을 타지 않는다.
     * 따라서 주석처리되어있는 경우 First, Second 모두 트랜잭션이 활성화되지 않는다. 이 때 Second에는 Transactional 어노테이션이 존재하므로 논리적으로 트랜잭션 선언은 되어있다. (활성화가 되지 않을뿐..)
     * 반면에 Transactional 어노테이션이 존재하는경우 기존 First의 트랜잭션에 참여하므로 총 1개의 트랜잭션이 활성화될 것이다.
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
     * 트랜잭션 전파범위는 SUPPORTS와 동일하나 기존 트랜잭션이 없으면 IllegalTransactionStateException 예외가 발생한다는 차이가 있다.
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
     * 총 1개의 물리적 트랜잭션이 동작한다.
     * FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석 처리하자.
     * 다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이 동작할까?
     * 0개
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     * https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-propagation.html
     * 논리적 트랜잭션은 각 메서드별로 생성된다. 이는 개별적으로 롤백 상태를 지정할 수 있다. PROPAGATION 설정에 따라 트랜잭션의 영향범위가 독립적일수도, 아닐수도 있다.
     * 물리적 트랜잭션은 연관된 논리적 트랜잭션들을 하나로 통합하여 관리하게된다.
     */
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported");
    }

    /**
     * 아래 테스트는 왜 실패할까?
     * Propagation의 NESTED 옵션은 부모의 속성을 상속받는다.
     * 부모의 롤백, 에러에는 영향을 받지만 반대로 NESTED가 선언된 자식 트랜잭션의 예외, 롤백은 부모에게 영향을 미치지 못한다.
     * 하지만 JPA에서는 NESTED 속성을 지원하지 않기 때문에 예외가 발생한다.
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     * 해당 속성은 기존 트랜잭션이 존재하지않으면 새롭게 트랜잭션을 생성한다.
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
     * NEVER 속성은 기존에 트랜잭션이 존재하면 IllegalTransactionStateException 예외를 발생시킨다.
     * 기존 트랜잭션이 없는 경우 트랜잭션 없이 작업을 수행한다.
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
