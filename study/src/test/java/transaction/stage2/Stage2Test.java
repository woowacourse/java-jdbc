package transaction.stage2;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.NestedTransactionNotSupportedException;

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
     */
    @Test
    void testRequiredNew() {
        final var actual = firstUserService.saveFirstTransactionWithRequiredNew();

        log.info("transactions : {}", actual);
        assertThat(actual).containsExactlyInAnyOrder(
                "transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew",
                "transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew");
        assertThat(userRepository.count()).isEqualTo(2);
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

        assertThat(firstUserService.findAll()).hasSize(1);
    }

    /**
     * FirstUserService.saveFirstTransactionWithSupports() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     */
    @Test
    void testSupports() {
        final var actual = firstUserService.saveFirstTransactionWithSupports();

        log.info("transactions : {}", actual);
        // SUPPORTS는 실제 트랜잭션 없이도 동기화 컨텍스트에 이름을 설정한다.
        assertThat(actual)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithSupports");
        assertThat(userRepository.count()).isEqualTo(2);
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     */
    @Test
    void testMandatory() {
        assertThatThrownBy(() -> firstUserService.saveFirstTransactionWithMandatory())
                .isInstanceOf(IllegalTransactionStateException.class);

        // 바깥 트랜잭션이 없어 첫 번째 저장은 이미 커밋된다.
        assertThat(userRepository.count()).isEqualTo(1);
    }

    /**
     * 아래 테스트는 몇 개의 물리적 트랜잭션이 동작할까?
     * FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석 처리하자.
     * 다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이 동작할까?
     *
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     */
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        // 서로 다른 컨텍스트의 이름이며, 이름 개수가 물리적 트랜잭션 개수는 아니다.
        assertThat(actual).containsExactlyInAnyOrder(
                "transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported",
                "transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported");
        assertThat(userRepository.count()).isEqualTo(2);
    }

    /**
     * 아래 테스트는 왜 실패할까?
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     */
    @Test
    void testNested() {
        assertThatThrownBy(() -> firstUserService.saveFirstTransactionWithNested())
                .isInstanceOf(NestedTransactionNotSupportedException.class);

        assertThat(userRepository.count()).isZero();
    }

    /**
     * 마찬가지로 @Transactional을 주석처리하면서 관찰해보자.
     */
    @Test
    void testNever() {
        assertThatThrownBy(() -> firstUserService.saveFirstTransactionWithNever())
                .isInstanceOf(IllegalTransactionStateException.class);

        assertThat(userRepository.count()).isZero();
    }
}
