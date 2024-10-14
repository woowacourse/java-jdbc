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
     * 풀이 : @Transactional(propagation = Propagation.REQUIRED) 설정이므로 물리적으로 1개의 트랜잭션을 사용하기에 1입니다.
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
     * 풀이 : @Transactional(propagation = Propagation.REQUIRES_NEW) 설정이므로 외부 트랜잭션과 분리되기에 물리적으로 2개의 트랜잭션을 사용하게 됩니다.
     */
    @Test
    void testRequiredNew() {
        final var actual = firstUserService.saveFirstTransactionWithRequiredNew();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew", "transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew");
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는 지 확인해보자.
     * 내부 REQUIRES_NEW 속성으로 트랜잭션이 새로 생성되지만, 강제로 예외를 발생시켜 새롭게 생성된 트랜잭션은 롤뱁됩니다.
     * 하지만 첫 번째 트랜잭션은 롤백되지 않기 때문에 저장이 제대로 수행되어 1이 조회됩니다.
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
     * 주석을 하고 실행하면 "SecondUserService.saveSecondTransactionWithSupports"의 트랜잭션이 잡히고, 주석을 해제하면
     * transaction.stage2.FirstUserService.saveFirstTransactionWithSupports의 트랜잭션이 잡힙니다.
     * 주석이 되면 트랜잭션 없이 실행되므로 FirstUserService에서 트랜잭션이 잡히지 않기 때문입니다.
     * 하지만 주석이 없다면 FirstUserService에서 이미 트랜잭션이 생성되기 때문에, SecondUserService도 해당 트랜잭션에 참여하게 됩니다.
     * 따라서 FirstUserService.saveFirstTransactionWithSupports 트랜잭션이 잡히게 됩니다.
     */
    @Test
    void testSupports() {
        final var actual = firstUserService.saveFirstTransactionWithSupports();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(0)
                .containsExactly("");
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     * 주석이 있다면, FirstUserService에서 트랜잭션이 생성되지 않기 때문에, SecondUserService.saveSecondTransactionWithMandatory()가
     * 호출될 때 상위 트랜잭션이 존재하지 않게 되어 IllegalTransactionStateException 예외가 발생하게 됩니다.
     * 만약 주석이 없다면, 정상적으로 상위 트랜잭션이 생성되기 때문에 예외가 발생하지 않습니다.
     * SUPPORTS 전파 속성은 상위 트랜잭션이 있으면 참여하고, 없으면 트랜잭션 없이 실행됩니다.
     * MANDATORY 전파 속성은 상위 트랜잭션이 무조건 있어야 하며, 없으면 예외를 발생시킵니다.
     */
    @Test
    void testMandatory() {
        final var actual = firstUserService.saveFirstTransactionWithMandatory();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(0)
                .containsExactly("");
    }

    /**
     * 아래 테스트는 몇 개의 물리적 트랜잭션이 동작할까?
     * FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석 처리하자.
     * 다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이 동작할까?
     *
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     *
     * NOT_SUPPORTED는 트랜잭션이 있는 경우 해당 트랜잭션을 일시 중단하고 트랜잭션 없이 작업을 수행합니다.
     *
     * 주석이 걸려있다면 물리적으로 트랜잭션이 전혀 발생하지 않지만, 주석이 해제된다면 생성됩니다.
     */
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly("");
    }

    /**
     * 아래 테스트는 왜 실패할까?
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     *
     * NESTED는 해당 메서드가 상위 트랜잭션 안에서 실행될 경우, 상위 트랜잭션에 종속된 중첩 트랜잭셩이 생성됩니다. 하지만 만약 상위 트랜잭션이 없다면
     * 기본적으로 트랜잭션을 생성하지 않기 때문에 중첩 트랜잭션을 만들 수 없어 예외가 발생할 수 있습니다.
     *
     * 주석을 걸면 잘 실행되지만, 해제하면 테스트는 깨지게됩니다.
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
     * NEVER 전파 속성은 현재 진행 중인 트랜잭션이 없을 때만 메서드를 실행하며, 만약 상위 트랜잭션이 존재하면 예외를 발생시킵니다.
     *
     * 주석이 없으면 테스트가 잘 실행되지만, 주석이 존재하면 테스트는 예외 발생과 함께 실패합니다.
     */
    @Test
    void testNever() {
        final var actual = firstUserService.saveFirstTransactionWithNever();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithNever");
    }
}
