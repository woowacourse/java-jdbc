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
     * 생성된 트랜잭션이 몇 개인가? -> 1개
     * 왜 그런 결과가 나왔을까? ->
     * saveFirstTransactionWithRequired 메서드의 @Transactional 애너테이션에 propagation = Propagation.REQUIRED 옵션이 설정되어 있어서,
     * 최초에 진행중인 트랜잭션이 존재하지 않아서 새로운 트랜잭션이 열린다.
     * 두 번째로 saveFirstTransactionWithRequired 메서드 내부에서 saveSecondTransactionWithRequired 메서드가 호출되는데,
     * 마찬가지로 propagation = Propagation.REQUIRED 옵션이 설정되어 있어서 첫 번째 메서드에서 시작된 기존 트랜잭션에 참여한다.
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
     * saveFirstTransactionWithRequiredNew -> propagation = Propagation.REQUIRED
     * saveSecondTransactionWithRequiresNew -> propagation = Propagation.REQUIRES_NEW
     * 첫 번째 메서드에서 propagation = Propagation.REQUIRED 옵션이 설정되어 있어서 기존에 열려있는 트랜잭션이 없기 때문에 새로운 트랜잭션 생성.
     * 두 번째 메서드에서 propagation = Propagation.REQUIRES_NEW 옵션이 설정되어 있어서 기존에 트랜잭션을 보류하고 새로운 트랜잭션 생성.
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
     * @Transactional(propagation = Propagation.REQUIRED)
     *     public Set<String> saveAndExceptionWithRequiredNew() {
     * @Transactional(propagation = Propagation.REQUIRES_NEW)
     *     public String saveSecondTransactionWithRequiresNew() {
     *
     *     saveAndExceptionWithRequiredNew 메서드에서 새로운 트랜잭션 생성.
     *     내부에서 saveSecondTransactionWithRequiresNew 메서드 호출하는데, 기존의 트랜잭션을 보류하고 새로운 트랜잭션 생성.
     *     saveSecondTransactionWithRequiresNew 메서드에서 생성한 별도의 트랜잭션에서 user insert 후 트랜잭션 종료.
     *     saveAndExceptionWithRequiredNew 메서드에서 트랜잭션 커밋되지 않고 롤백.
     *     결과적으로 첫 번째 메서드에서 롤백이 발생했지만, 두 번째 메서드에서 생성된 새로운 트랜잭션은 별도의 트랜잭션이라
     *     롤백되지 않고 정상적으로 커밋됨. 그래서 첫 번째 트랜잭션이 롤백 됐음에도 1명의 유저가 조회됨.
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
     * 첫 번째 메서드의 트랜잭션이 주석인 상태에서 테스트를 실행하면 SecondUserService.saveSecondTransactionWithSupports 메서드에서
     * propagation = Propagation.SUPPORTS 옵션이므로 기존 트랜잭션이 있으면 참여하고, 없으면 트랜잭션 없이 진행한다.
     * 그래서 첫 번째 메서드에서 트랜잭션이 없는 상태로 두 번째 메서드 실행시 트랜잭션이 없다.
     * 반면에 첫 번째 메서드에서 propagation = Propagation.REQUIRED 옵션으로 진행시 두 번째 메서드에서
     * 첫 번째 메서드에서 생성한 트랜잭션에 참여한다.
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
     * 첫 번째 메서드에서 Propagation.REQUIRED 옵션으로 트랜잭션이 생성된 상황에 내부적으로 두 번째 메서드를 호출하면,
     * Propagation.MANDATORY 옵션으로 필수적으로 트랜잭션이 있어야 예외가 발생하지 않는다.
     *
     * Propagation.SUPPORTS 옵션과 다른 점은, SUPPORTS 옵션은 기존 트랜잭션이 없으면 트랜잭션이 없는 채로 진행되는데,
     * Propagation.MANDATORY 옵션은 기존 트랜잭션이 없는 경우 예외를 발생시킨다.
     * IllegalTransactionStateException: No existing transaction found for transaction marked with propagation 'mandatory'
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
     * 첫 번째 메서드의 주석을 해제하면 Propagation.REQUIRED 옵션으로 새로운 트랜잭션이 생성되고,
     * 내부에서 두 번째 메서드에서 Propagation.NOT_SUPPORTED 옵션으로 인해 기존 트랜잭션이 보류시키고 트랜잭션을 사용하지 않게 한다.
     *
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     *
     * 물리적 트랜잭션은 실제 데이터베이스 트랜잭션과 상응하는 트랜잭션이다.
     * 논리적 트랜잭션은 스프링 트랜잭션 관리 메커니즘인 PlatformTransactionManager에 의해 관리되는 트랜잭션 단위이다.
     */
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactlyInAnyOrder("transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported", "transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported");
    }

    /**
     * 아래 테스트는 왜 실패할까?
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     * 주석 처리하면 SecondUserService.saveSecondTransactionWithNested() 메서드의 Propagation.NESTED 옵션으로 트랜잭션이 실행된다.
     *
     * FirstUserService.saveFirstTransactionWithNested() 메서드를 주석 처리하지 않고 실행시 예외가 발생한다.
     * NestedTransactionNotSupportedException: JpaDialect does not support savepoints - check your JPA provider's capabilities
     * 이 기능은 JDBC의 savepoint 기능을 사용하기 때문에 JPA 등에서는 사용이 불가능하다.
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
     * Propagation.NEVER 옵션은 기존 트랜잭션이 존재하면 예외를 발생시킨다.
     * 그리고 기존 트랜잭션이 존재하지 않으면, 트랜잭션이 활성화되지 않은 채로 진행된다.
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
