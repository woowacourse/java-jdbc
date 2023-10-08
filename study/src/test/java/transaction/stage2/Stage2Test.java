package transaction.stage2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.IllegalTransactionStateException;

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
     * 생성된 트랜잭션이 몇 개인가? 1개
     *
     * 왜 그런 결과가 나왔을까?
     * FirstUserService 의 saveFirstTransactionWithRequired() 메서드에서 생성한 트랜잭션을 사용하고 있기 때문에 1개.
     *
     * Required 옵션을 설정하고 @Transactional 애너테이션을 메서드 레벨에 붙이면
     * 해당 메서드를 호출한 곳에서 사용하던 기존 트랜잭션을 사용하고, 그게 아니라면 새로운 트랜잭션을 생성해서 사용한다.
     *
     * saveFirstTransactionWithRequired() 내부에서 호출한
     * SecondUserService의 saveSecondTransactionWithRequired() 메서드에 붙인 애너테이션의 propagation 옵션을 REQUIRED로 설정했기 때문에
     * 기존 트랜잭션을 그대로 사용한 것.
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
     *
     * 왜 그런 결과가 나왔을까?
     * FirstUserService의  saveFirstTransactionWithRequiredNew() 메서드에서 시작한 트랜잭션과
     * SecondUserService의 saveSecondTransactionWithRequiresNew() 메서드에서 시작한 트랜잭션을 모두 사용해서 2개다.
     *
     * SecondUserService의 saveSecondTransactionWithRequiresNew() 메서드에서 사용한 propagation.REQUIRES_NEW 옵션은
     * 매번 새로운 트랜잭션을 시작한다.
     * 메서드를 호출한 쪽의 트랜잭션이 있더라도 대기 상태로 두고 자신의 트랜잭션을 실행하며,
     * 독립적으로 동작한다.
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
     *
     * 내부에서 호출된 메서드에 propagation.REQUIRES_NEW 옵션설정 되어 있어서 부분적으로 롤백되는 현상 발생.
     * 호출한 곳의 트랜잭션과 호출된 곳의 트랜잭션이 독립적으로 동작하기 때문에 롤백도 호출한 곳의 트랜잭션에만 적용되었다.
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
     * 주석일 때는 non-transaction 으로 동작
     * (트랜잭션 활성화 x)
     * 아닐 때는 FirstUserService.saveFirstTransactionWithSupports 의 트랜잭션 사용
     * (호출한 곳의 트랜잭션으로 호출한 곳 작업과 호출된 곳 작업 모두 처리)
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
     *
     * 주석이면 예외를 발생시킨다.
     *
     * 주석 아니면 호출한 곳의 트랜잭션 사용
     */
    @Test
    void testMandatory() {
//        final var actual = firstUserService.saveFirstTransactionWithMandatory();

//        log.info("transactions : {}", actual);
//        assertThat(actual)
//                .hasSize(1)
//                .containsExactly("");

        assertThatThrownBy(
                () -> firstUserService.saveFirstTransactionWithMandatory()
        ).isInstanceOf(IllegalTransactionStateException.class);
    }

    /**
     * 아래 테스트는 몇 개의 물리적 트랜잭션이 동작할까? 2개의 트랜잭션이 존재하지만, 호출한 곳의 트랜잭션만 활성화 되어있음
     * FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석 처리하자.
     *
     * 다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이 동작할까?
     * 트랜잭션은 1개인데, 활성화되지는 않음 (non-transactional)
     *
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     */
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported",
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported");
    }

    /**
     * 1. 아래 테스트는 왜 실패할까?
     * 중첩 트랜잭션은 JDBC 3.0 이후 버전의 savepoint기능을 사용.
     * JPA를 사용하는 경우, 변경감지를 통해서 업데이트문을 최대한 지연해서 발행하는 방식을 사용하기 때문에
     * 중첩된 트랜잭션 경계를 설정할 수 없어 지원하지 않는다고 한다...?
     * 그래서 이와 같은 예외가 발생한다.
     * NestedTransactionNotSupportedException: JpaDialect does not support savepoints - check your JPA provider's capabilities
     *
     * 2. FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     * 주석 처리하면 호출된 곳의 트랜잭션 사용
     */
    @Test
    void testNested() {
        final var actual = firstUserService.saveFirstTransactionWithNested();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(0)
                .containsExactly("");
    }

    /**
     * 마찬가지로 @Transactional을 주석처리하면서 관찰해보자.
     *
     * 호출한 곳의 트랜잭션이 존재하면 예외가 발생
     * 존재하지 않으면 non-transactional 로 동작 (비활성 상태)
     */
    @Test
    void testNever() {
        final var actual = firstUserService.saveFirstTransactionWithNever();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(0)
                .containsExactly("");
    }
}
