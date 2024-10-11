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
     * 1개의 트랜잭션이 생성된다.
     * Required로 설정하면 이전에 열려있던 트랜잭션에 같이 참여하게 된다.
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
     * 2개의 트랜잭션이 생성된다.
     * REQUIRES_NEW로 설정하면 이전에 트랜잭션이 열려있더라도 별개의 트랜잭션을 연다.
     */
    @Test
    void testRequiredNew() {
        final var actual = firstUserService.saveFirstTransactionWithRequiredNew();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactlyInAnyOrder("transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew",
                        "transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew");
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는 지 확인해보자.
     *
     * 두 개의 트랜잭션이 다른 트랜잭션이므로, secondUserService.saveSecondTransactionWithRequiresNew() 에서 생성된 데이터는
     * 트랜잭션이 종료되면서 데이터가 저장되고, 이후의 데이터는 에러가 발생하면서 롤백된다.
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
     * 주석이 없을 때는 먼저 열렸던 FirstUserService.saveFirstTransactionWithSupports가 반환되지만,
     * 주석이 있을 때는 FirstUserService.saveFirstTransactionWithSupports가 열리지 않게 되고,
     * 해당 메소드 안에서 열리는 SecondUserService.saveSecondTransactionWithSupports가 반환된다.
     * 하지만 반환되는 건 맞지만 트랜잭션이 걸리는 것은 아니다.
     * SUPPORTS는 부모 트랜잭션이 있는 경우 해당 트랜잭션에 합류하고, 없는 경우에는 없는 상태로 진행되기 때문이다.
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
     * 주석이 있는 경우 예외가 발생하게 된다.
     * 그 이유는 secondUserService.saveSecondTransactionWithMandatory() 에 걸려있는 Mandatory 설정 때문인데,
     * 이 설정은 부모 트랜잭션이 없는 경우 IllegalTransactionStateException를 발생시킨다.
     * 부모 트랜잭션이 있는 경우에는 합류한다.
     * SUPPORTS와의 차이점은, SUPPORTS는 부모 트랜잭션이 없는 경우 없는 상태로 진행되지만, MANDATORY의 경우 예외가 발생한다.
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
     * 주석이 없으면 2개, 있으면 1개의 물리적 트랜잭션이 동작할 것 같다.
     * NOT_SUPPORTED 설정은 부모 트랜잭션이 없는 경우 트랜잭션이 없는 상태로 진행되고, 있으면 잠시 보류하고 트랜잭션 없이 진행된다.
     * 하지만 Spring Data JPA를 사용할 경우 save 메소드를 호출할 때 자동으로 트랜잭션을 적용해준다.
     * 따라서 주석이 없는 경우, 부모 트랜잭션이 동작하고, secondUserService.saveSecondTransactionWithNotSupported()이 실행될 때는 부모 트랜잭션이 보류된 후
     * 트랜잭션 없이 진행되는데, 이 때 save 메소드가 호출되며 자체적으로 트랜잭션이 적용되므로 총 2개의 물리적 트랜잭션이 동작하게 된다.
     * 주석이 있는 경우에는 부모 트랜잭션은 동작하지 않고, secondUserService.saveSecondTransactionWithNotSupported() 또한 트랜잭션 없이 진행되므로
     * save 메소드에 자체적으로 적용되는 트랜잭션만 존재하여 1개의 물리적 트랜잭션이 동작하게 된다.
     *
     * 물리적 트랜잭션: 실제 데이터 베이스에 적용되는 개념
     * 논리적 트랜잭션: 스프링 프레임워크 내부에서 트랜잭션의 경계를 정의하고 제어하는 개념
     */
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactlyInAnyOrder("transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported",
                        "transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported");
    }

    /**
     * 아래 테스트는 왜 실패할까?
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     *
     * NESTED는 부모 트랜잭션이 존재할 경우 그 내부에서 서브 트랜잭션을 생성한다.
     * 이 서브 트랜잭션은 부모 트랜잭션의 세이브포인트를 설정하여 부모와 연관된 상태를 관리한다..
     * 서브 트랜잭션이 롤백되면 부모 트랜잭션의 세이브포인트를 기준으로 서브 트랜잭션에서 변경된 내용만 롤백되며, 부모 트랜잭션은 그대로 유지된다.
     * 이 세이브포인트 기능은 JDBC의 커넥션에서만 지원하는데, 하이버네이트는 이를 지원하지 않아 예외가 발생한다.
     * 하지만 부모 트랜잭션이 없는 경우 단순하게 새로운 트랜잭션을 생성하므로 예외가 발생하지 않고 잘 동작하게 된다.
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
     * NEVER 설정은 MANDATORY와는 반대로, 부모 트랜잭션이 없는 경우에만 동작한다.
     * 부모 트랜잭션이 있는 경우 IllegalTransactionStateException가 발생하며
     * 없는 경우에는 트랜잭션 없이 그대로 진행되게 된다.
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
