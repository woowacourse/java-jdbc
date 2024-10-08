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
     * 생성된 트랜잭션이 몇 개인가? 1개
     * 왜 그런 결과가 나왔을까?
     * REQURIED 전파 레벨은 기존의 트랜잭션이 있으면 참가하고 없으면 새로 생성한다.
     * 따라서 SecondUserService의 saveSecondTransactionWithRequired는
     * FirstUserService의 saveFirstTransactionWithRequried 메서드에서 열린 트랜잭션에 참가한다.
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
     * 왜 그런 결과가 나왔을까?
     * SecondUserService의 설정인 REQUIRES_NEW는 기존의 트랜잭션이 있어도 새로 생성해 독자적인 트랜잭션을 시작하기 때문이다.
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
     * SecondUserService의 saveSecondTransactionWithRequiresNew는 독자적인 트랜잭션에서 실행되기 때문에 이미 커밋된 후
     * FirstUserService의 rollback이 발생한다.
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
     * FirstUserService의 주석을 해제하면 트랜잭션이 1개가 되고 SecondUserService의 트랜잭션이 Active 상태가 된다.
     * FirstUserService의 주석이 남아있으면 SecondUserService가 트랜잭션을 생성은 하지만 active 상태가 아니다.
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
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     * FistUserService의 주석이 없으면 SecondUserService에서 예외가 발생한다. MANDATORY는 참여할 트랜잭션이 없으면 예외가 발생한다.
     * SUPPORTS는 MANDATORY와 달리 예외가 발생하지 않고 ACTIVE하지 않은 트랜잭션을 만든다.
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
     * 아래 테스트는 몇 개의 물리적 트랜잭션이 동작할까? 2개
     * FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석 처리하자.
     * 다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이 동작할까? 1개
     * NOT_SUPPORTED는 트랜잭션이 없는 것처럼 동작한다. 하지만 그럼에도 불구하고 논리적 트랜잭션(Active 상태가 아닌)은 생성되기 때문에 1개는 생성된다.
     * 또한 트랜잭션이 없는 것처럼 동작하게하기 위해 논리적 트랜잭션을 생성해 분리하기 때문에
     * 부모의 트랜잭션이 존재하면 부모의 1개 + NOT_SUPPORTED의 1개해서 2개가 존재한다.
     *
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     * 물리적 트랜잭션 - 실제로 DB에 적용되는 트랜잭션 단위
     * 논리적 트랜잭션 - 스프링이 트랜잭션 매니저를 통해 생성하는 트랜잭션. @Transactional 어노테이션이 달려있으면 논리적 트랜잭션이 생성된다.
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
     * JPA에서 NESTED에 대한 동작을 지원하지 않기 때문에 예외가 발생한다.
     * 주석처리 할 경우 SecondTransaction의 트랜잭션이 새로 생성되기 때문에(REQUIRED처럼 동작) 1개의 트랜잭션이 감지된다.
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
     * NEVER 레벨은 트랜잭션 없이 동작하고 싶어하고, 기존 트랜잭션이 존재하면 에외를 발생하기 때문에 FirstUserService에 트랜잭션이 존재하면 예외가 밸상한다.
     * 하지만 NEVER 역시 ACTIVE하지 않은 논리적 트랜잭션은 생성되기 때문에 1개의 트랜잭션이 감지된다.
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
