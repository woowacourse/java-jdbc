package transaction.stage2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionSynchronizationManager;

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
     * 생성된 트랜잭션이 몇 개인가? -> 1개
     * 왜 그런 결과가 나왔을까? -> 트랜잭션 전파 설정이 모두 required이기 때문에 같은 트랜잭션 사용
     */
    @Test
    void testRequired() {
        final var actual = firstUserService.saveFirstTransactionWithRequired();

        String[] transactions = actual.toArray(new String[0]);

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly(transactions[0]);
    }

    /**
     * 생성된 트랜잭션이 몇 개인가? -> 2개
     * 왜 그런 결과가 나왔을까? 내부 트랜잭션의 전파 속성이 requires_new 이기 때문에
     * 외부 트랜잭션과 별개로 새로운 트랜잭션을 실행하여 총 2개
     */
    @Test
    void testRequiredNew() {
        final var actual = firstUserService.saveFirstTransactionWithRequiredNew();

        String[] transactions = actual.toArray(new String[0]);

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly(transactions[0], transactions[1]);
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는 지 확인해보자.
     *
     * -> 내부 트랜잭션이 REQUIRES_NEW이기 때문에, 새로운 커넥션을 얻어 트랜잭션을 시작한다.
     * 그래서 내부 트랜잭션이 롤백되더라도 외부 트랜잭션에는 영향을 미치지 않는다.
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
     * -> 내부 트랜잭션의 전파속성이 SUPPORT이다.
     * SUPPORT 전파 속성은 외부 트랜잭션이 있으면 참여하고, 없으면 트랜잭션 없이 진행한다.
     * 주석 상태로 외부 트랜잭션이 없는 경우는 내부트랜잭션도 시작되지 않고,
     * 주석을 풀면 외부 트랜잭션에 내부트랜잭션이 참여하게 된다.
     */
    @Test
    void testSupports() {
        final var actual = firstUserService.saveFirstTransactionWithSupports();

        String[] transactions = actual.toArray(new String[0]);

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly(transactions[0]);
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     *
     * -> MANDATORY는 외부 트랜잭션이 무조건 존재해야하고, 없으면  IllegalTransactionStateException를 발생시킨다.
     * SUPPORT와 다른 점은 MANDATORY는 외부 트랜잭션이 필수이지만 SUPPORT는 아니라는 것이다.
     * 둘다 트랜잭션이 존재하면 참여하는 것은 동일하다.
     */
    @Test
    void testMandatory() {
        final var actual = firstUserService.saveFirstTransactionWithMandatory();

        String[] transactions = actual.toArray(new String[0]);

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly(transactions[0]);
    }

    /**
     * 아래 테스트는 몇 개의 물리적 트랜잭션이 동작할까?
     * FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석 처리하자.
     * 다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이 동작할까?
     * -> 0개, 외부 트랜잭션이 존재하지 않고, NOT_SUPPORTED은 트랜잭션을 지원하지 않기 떄문이다.
     *
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     *
     * -> 물리적 트랜잭션 : 실제 커넥션을 얻어 실행하는 단위의 트랜잭션
     * 논리적 트랜잭션 : 스프링에서 트랜잭션 매니저를 통해 처리하는 트랜잭션 단위
     */
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        String[] transactions = actual.toArray(new String[0]);

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly(transactions[0]);
    }

    /**
     * 아래 테스트는 왜 실패할까?
     *
     * NESTED는 중첩 트랜잭션을 사용하여 부모 트랜잭션에 자식 트랜잭션을 생성한다.
     * 부모 트랜잭션의 결과는 자식 트랜잭션에 영향을 주지만, 자식 트랜잭션은 부모 트랜잭션에 영향을 주지 않는다.
     * NESTED는 JDBC의 savepoint 기능을 사용하는데, JPA에서는 해당 기능을 지원하지 않아 오류를 던진다.
     *
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     *
     * 외부 트랜잭션이 없으면 자식 트랜잭션을 생성하는 것이 아닌, 새로운 트랜잭션을 시작하기 떄문에 오류가 발생하지 않는다.
     */
    @Test
    void testNested() {
        final var actual = firstUserService.saveFirstTransactionWithNested();

        String[] transactions = actual.toArray(new String[0]);

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly(transactions[0]);
    }

    /**
     * 마찬가지로 @Transactional을 주석처리하면서 관찰해보자.
     *
     * -> Never 설정의 경우 트랜잭션을 적용하지 않겠다는 의미이다.
     * 외부 트랜잭션이 존재하면 IllegalTransactionStateException을 발생시키고
     * 외부 트랜잭션이 없다면 트랜잭션을 적용하지 않는다.
     */
    @Test
    void testNever() {
        final var actual = firstUserService.saveFirstTransactionWithNever();

        String[] transactions = actual.toArray(new String[0]);

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly(transactions[0]);
    }
}
