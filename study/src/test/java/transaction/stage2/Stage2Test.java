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
     */
    @Test
    void testRequired() {
        final var actual = firstUserService.saveFirstTransactionWithRequired();


        // REQUIRED 이므로 1개 그대로 사용
        // 처음 생성한 트랜잭션 으로 유지

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

        // REQUIRED_NEW : 필요하다면 새로 생성
        // OSIV 가 FASLE 이면, 당연히 두 개의 커넥션 연결
        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsAnyOf(
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew",
                        "transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew");
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는 지 확인해보자.
     */
    @Test
    void testRequiredNewWithRollback() {
        assertThat(firstUserService.findAll()).hasSize(0);

        // 첫번째 트랜잭션에서 예외를 발생해도 두번째 트랜잭션은 롤백되지 않는다.
        // REQUIRED_NEW 로 인한 새로운 트랜잭션이 커밋되며 롤백을 해도 롤백되지 않는다.
        // 첫번째 트랜잭션은 예외로 인해 롤백
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

        // 어노테이션을 주석하면, 트랜잭션 단위가 생성되지 않는다.
        // null is Actual Transaction Active : ❌ false
        // SUPPORTS : 기존 트랜잭션이 있다면 참여하고, 없으면 참여하지 않는다.
        log.info("transactions : {}", actual);

        //TransactionSynchronizationManager.getCurrentTransactionName 를 통해 보면, 이름은 반환하나 트랜잭션 활성화 되어있지 않다.

        // 주석을 해제하면, 이름도 first 로 반환 + 활성화 되어 있다. ( 즉 참여한다는 의미 )
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithSupports");
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     */
    @Test
    void testMandatory() {

        // MANDATORY : 이미 진행 중인 Transaction이 있으면 참여한다. 반면에 기존에 생성된 Transaction 이 없다면 예외를 발생시킨다.
//        assertThatThrownBy(() -> firstUserService.saveFirstTransactionWithMandatory())
//                .isInstanceOf(IllegalTransactionStateException.class);

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
     * <p>
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     */
    @Test
    void testNotSupported() {
        // NOT_SUPPORT : 기존에 생성된 Transaction 이 있든 말든 Transaction 없이 진행
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();


//        transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported is Actual Transaction Active : ✅ true
//        transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported is Actual Transaction Active : ❌ false

//        null is Actual Transaction Active : ❌ false
//        transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported is Actual Transaction Active : ❌ false
        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsAnyOf(
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported",
                        "transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported");
    }

    /**
     * 아래 테스트는 왜 실패할까?
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     */
    @Test
    void testNested() {
        //NESTED : 이미 진행 중인 Transaction이 있다면 중첩으로 Transaction이을 생성해서 진행

        //주석 시
//        null is Actual Transaction Active : ❌ false
//        transaction.stage2.SecondUserService.saveSecondTransactionWithNested is Actual Transaction Active : ✅ true

        // 주석 하지 않을 시
//        transaction.stage2.FirstUserService.saveFirstTransactionWithNested is Actual Transaction Active : ✅ true
//        예외 발생

        //변경감지를 통해서 업데이트문을 최대한 지연해서 발행하는 방식을 사용하기 때문에 중첩된 트랜잭션 경계를 설정할 수 없어 지원하지 않는다.
//        assertThatThrownBy(()->firstUserService.saveFirstTransactionWithNested())
//                .isInstanceOf(NestedTransactionNotSupportedException.class);

        final var actual = firstUserService.saveFirstTransactionWithNested();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNested");
    }

    /**
     * 마찬가지로 @Transactional을 주석처리하면서 관찰해보자.
     */
    @Test
    void testNever() {

        //NEVER : 트랜잭션이 존재하는 경우에 예외를 발생시켜 트랜잭션을 사용하지 않는 것을 강제하는 속성입니다.

        // 주석 하지 않을 시
//        assertThatThrownBy(()->firstUserService.saveFirstTransactionWithNever())
//                .isInstanceOf(IllegalTransactionStateException.class);

        // 주석 할 시
//        null is Actual Transaction Active : ❌ false
//        transaction.stage2.SecondUserService.saveSecondTransactionWithNever is Actual Transaction Active : ❌ false

        final var actual = firstUserService.saveFirstTransactionWithNever();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNever");
    }
}
