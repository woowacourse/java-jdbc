package transaction.stage2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.NestedTransactionNotSupportedException;

/**
 * 트랜잭션 전파(Transaction Propagation)란? 트랜잭션의 경계에서 이미 진행 중인 트랜잭션이 있을 때 또는 없을 때 어떻게 동작할 것인가를 결정하는 방식을 말한다.
 * <p>
 * FirstUserService 클래스의 메서드를 실행할 때 첫 번째 트랜잭션이 생성된다. SecondUserService 클래스의 메서드를 실행할 때 두 번째 트랜잭션이 어떻게 되는지 관찰해보자.
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
     * 생성된 트랜잭션이 몇 개인가? 왜 그런 결과가 나왔을까?
     * 1개, Propagation.REQUIRED는 진행 중인 트랜잭션이 없으면 새로 시작하고, 있으면 참여한다.
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
     * 생성된 트랜잭션이 몇 개인가? 왜 그런 결과가 나왔을까?
     * 2개, REQUIRES_NEW는 이미 진행중인 트랜잭션이 있어도 새로운 트랜잭션을 생성하기 때문이다.
     * saveSecondTransactionWithRequiresNew -> saveFirstTransactionWithRequiredNew 로 2개 트랜잭션 생성
     */
    @Test
    void testRequiredNew() {
        final var actual = firstUserService.saveFirstTransactionWithRequiredNew();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew"
                        , "transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew");
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는 지 확인해보자.
     * REQUIRES_NEW는 새로운 트랜잭션으로 취급되서 기존 REQUIRED의 트랜잭션에 롤백이 되지 않는다.
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
     * SUPPORTS은 진행중인 트랜잭션이 있으면 지원하고 없으면 트랜잭션 없이 동작한다.
     */
    @Test
    void testSupports() {
        final var actual = firstUserService.saveFirstTransactionWithSupports();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithSupports");
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다. 주석인 상태에서 테스트를 실행했을 때와
     * 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자. SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     * MANDATORY는 이미 진행 중인 트랜잭션이 있으면 참여하고, 진행 중인 트랜잭션이 없으면 예외를 발생시킨다.
     */
    @Test
    void testMandatory() {
        assertThatThrownBy(() -> firstUserService.saveFirstTransactionWithMandatory())
                .isInstanceOf(IllegalTransactionStateException.class);
    }

    /**
     * 아래 테스트는 몇 개의 물리적 트랜잭션이 동작할까?
     * FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석 처리하자.
     * 다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이 동작할까?
     *
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     * NOT_SUPPORTED는 트랜잭션 없이 실행되는 방식으로, 진행 중인 트랜잭션이 있으면 일시 중단(suspend)하고 실행한다.
     * 주석 해제: First의 물리적 트랜잭션 1개. Second는 First의 트랜잭션을 중단시키고 빈 트랜잭션으로 실행된다.
     * 주석 처리: 물리적 트랜잭션 0개. First는 트랜잭션이 없고, Second도 빈 트랜잭션으로 실행된다.
     * 물리적 트랜잭션은 실제 DB 커넥션에서 시작되고 커밋/롤백되는 트랜잭션이다.
     * 논리적 트랜잭션은 @Transactional이 적용된 메서드마다 생기는 트랜잭션 범위로, 여러 논리적 트랜잭션이 하나의 물리적 트랜잭션에 참여할 수 있다.
     */
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactlyInAnyOrder("transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported"
                        , "transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported");
    }

    /**
     * 아래 테스트는 왜 실패할까?
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     * Nested는 이미 진행중인 트랜잭션이 있을 때 중첩 트랜잭션을 생성하는 방식.
     * 진행중인 트랜잭션이 없는 경우 REQUIRED 처럼 새로운 트랜잭션을 생성한다.
     * 기존 트랜잭션이 있으면 NESTED는 savepoint를 만들려고 하는데, JpaTransactionManger()는 savepoint를 지원하지 않아서 NestedTransactionNotSupportedException 예외가 발생한다.
     * 기존 트랜잭션이 없으면 NESTED는 REQUIRED처럼 새 트랜잭션을 시작한다.
     */
    @Test
    void testNested() {
        assertThatThrownBy(() -> firstUserService.saveFirstTransactionWithNested())
                .isInstanceOf(NestedTransactionNotSupportedException.class);
    }

    /**
     * 마찬가지로 @Transactional을 주석처리하면서 관찰해보자.
     * NEVER는 트랜잭션 없이 실행되어야 하는 방식으로, MANDATORY와 반대다.
     * 기존 트랜잭션이 있으면 IllegalTransactionStateException 예외가 발생한다.
     * 기존 트랜잭션이 없으면 SUPPORTS처럼 트랜잭션 없이(빈 트랜잭션) 실행된다.
     */
    @Test
    void testNever() {
        assertThatThrownBy(() -> firstUserService.saveFirstTransactionWithNever())
                .isInstanceOf(IllegalTransactionStateException.class);
    }
}
