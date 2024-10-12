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
     * 생성된 트랜잭션이 몇 개인가? => 1
     * 왜 그런 결과가 나왔을까?
     * REQUIRED : 트랜잭션이 존재하는 경우 해당 트랜잭션 사용하고, 트랜잭션이 없는 경우 트랜잭션을 생성
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
     * 생성된 트랜잭션이 몇 개인가? => 2
     * 왜 그런 결과가 나왔을까?
     * REQUIRED_NEW : 트랜잭션이 존재하는 경우 트랜잭션을 잠시 보류시키고, 신규 트랜잭션을 생성하여 사용
     */
    @Test
    void testRequiredNew() {
        final var actual = firstUserService.saveFirstTransactionWithRequiredNew();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactlyInAnyOrder("transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew",
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew");
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는 지 확인해보자.
     * 부모 트랙잭션 롤백 : 자식 트랜잭션은 롤백 X
     * 자식 트랜잭션 롤백 : 부모 트랜잭션은 롤백 X
     * 자식 트랜잭션에서 발생한 예외가 부모 트랜잭션에서 같이 던져지는 경우 : 두 트랜잭션 모두 롤백
     * <p>
     * 부모 트랜잭션 롤백, 자식 트랜잭션 롤백 X
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
     * <p>
     * SUPPORT : 트랜잭션이 존재하는 경우 트랜잭션을 사용, 트랜잭션이 없다면 트랜잭션 없이 실행
     * <p>
     * 주석있는 경우 : 트랜잭션 없이 실행(saveSecondTransactionWithSupports is Actual Transaction Active : ❌ false)
     * 주석없는 경우 : 트랜잭션 합류(saveFirstTransactionWithSupports is Actual Transaction Active : ✅ true)
     */
    @Test
    void testSupports() {
        final var actual = firstUserService.saveFirstTransactionWithSupports();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsOnly("transaction.stage2.SecondUserService.saveSecondTransactionWithSupports");
//                .containsOnly("transaction.stage2.FirstUserService.saveFirstTransactionWithSupports");
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     * <p>
     * MANDATORY : 트랜잭션이 반드시 있어야 함
     * 주석있는 경우 : IllegalTransactionStateException 예외 발생(No existing transaction found for transaction marked with propagation 'mandatory')
     * 주석없는 경우 : 트랜잭션 합류(saveFirstTransactionWithMandatory is Actual Transaction Active : ✅ true)
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
     * 아래 테스트는 몇 개의 물리적 트랜잭션이 동작할까? => 1
     * FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석 처리하자.
     * 다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이 동작할까? => 0
     * <p>
     * NOT_SUPPORTED: 트랜잭션이 존재하는 경우 트랜잭션을 잠시 보류, 트랜잭션이 없는 상태로 처리
     * 주석있는 경우(0) : 트랜잭션이 없는 상태로 처리(saveSecondTransactionWithNotSupported is Actual Transaction Active : ❌ false)
     * 주석없는 경우(1) : 부모 트랜잭션 보류, 트랜잭션 없는 상태로 처리
     * <p>
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     * 물리 트랜잭션: 실제 데이터베이스에 적용되는 트랜잭션으로, 커넥션을 이용해 커밋/롤백하는 단위
     * 논리 트랜잭션: 스프링이 트랜잭션 매니저를 통해 트랜잭션을 처리하는 단위
     * 실제 데이터베이스 트랜잭션과 스프링이 처리하는 트랜잭션 영역을 구분이 목적
     */
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsOnly("transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported");
    }

    /**
     * 아래 테스트는 왜 실패할까?
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     * <p>
     * 주석있는 경우 : 트랜잭션 생성(saveSecondTransactionWithNested is Actual Transaction Active : ✅ true)
     * 주석없는 경우 : NestedTransactionNotSupportedException(중첩 트랜잭션 지원하지 않음)
     * <p>
     * NESTED : 트랜잭션이 있다면 SAVEPOINT를 남기고 중첩 트랜잭션을 시작, 없다면 새로운 트랜잭션을 시작
     * 하위 메서드의 트랜잭션이 커밋되어도 기존 트랜잭션이 롤백되면, 전체 트랜잭션 롤백
     * 하위 메서드의 작업이 롤백된 경우 SAVEPOINT를 남긴 부분까지 부분 롤백
     * REQUIRES_NEW는 부모 트랜잭션과 자식 트랜잭션이 모두 독립적으로 커밋/롤백 가능
     * 반면, NESTED의 커밋은 부모 트랜잭션과 함께 진행되고 롤백은 부분적으로 가능
     * <p>
     */
    @Test
    void testNested() {
        final var actual = firstUserService.saveFirstTransactionWithNested();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsOnly("transaction.stage2.SecondUserService.saveSecondTransactionWithNested");
    }

    /**
     * 마찬가지로 @Transactional을 주석처리하면서 관찰해보자.
     * <p>
     * NEVER : 트랜잭션이 있다면 예외, 없어도 생성하지 않음
     * <p>
     * 주석있는 경우 : 트랜잭션이 없는 상태로 처리(saveSecondTransactionWithNever is Actual Transaction Active : ❌ false)
     * 주석없는 경우 : IllegalTransactionStateException(Existing transaction found for transaction marked with propagation 'never')
     */
    @Test
    void testNever() {
        final var actual = firstUserService.saveFirstTransactionWithNever();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsOnly("transaction.stage2.SecondUserService.saveSecondTransactionWithNever");
    }
}
