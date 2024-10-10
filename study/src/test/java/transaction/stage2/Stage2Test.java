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
        // REQUIRED는 진행중인 트랜잭션이 없으면 새로 시작하고, 있다면 참여한다.
        // first에서 진행중인 트랜잭션이 있기 때문에, second는 first의 트랜잭션에 참여한다.
    }

    /**
     * 생성된 트랜잭션이 몇 개인가?
     * 왜 그런 결과가 나왔을까?
     */
    @Test
    void testRequiredNew() {
        final var actual = firstUserService.saveFirstTransactionWithRequiredNew();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly(
                        "transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew",
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew");
        // REQUIRES_NEW는 항상 새로운 트랜잭션을 시작한다.
        // first에서 진행중인 트랜잭션이 있지만, second는 REQUIRES_NEW로 설정되어 있기 때문에
        // first의 트랜잭션 존재 여부와 관계 없이 새로운 트랜잭션을 시작한다.
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
        // first와 second는 모두 User save를 시도하고 있다.
        // second를 REQUIRES_NEW로 설정했기 때문에, 별개의 트랜잭션에서 save가 발생한다.
        // first에서 예외가 발생하면서, first는 롤백되었지만
        // second는 별개의 트랜잭션이므로 롤백되지 않고 데이터가 save된 상태로 남아 있다.
    }

    /**
     * FirstUserService.saveFirstTransactionWithSupports() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     */
    @Test
    void testSupports() {
        final var actual = firstUserService.saveFirstTransactionWithSupports();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithSupports");
        // SUPPORTS 옵션: 기존 트랜잭션이 있으면 참여, 없으면 트랜잭션 없이 실행

        // (1) first에 트랜잭션 X, second에 트랜잭션 O (SUPPORTS)
        // 트랜잭션을 사용하지 않음. 트랜잭션이 active하지 않음.

        // (2) fisrt에 트랜잭션 O, second에 트랜잭션 O (SUPPORTS)
        // second가 first 트랜잭션에 참여하면서, 트랜잭션명이 first로 나타나고 트랜잭션이 active함.
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     */
    @Test
    void testMandatory() {
//        assertThatThrownBy(() -> firstUserService.saveFirstTransactionWithMandatory())
//                .isInstanceOf(IllegalTransactionStateException.class)
//                .hasMessage("No existing transaction found for transaction marked with propagation 'mandatory'");

        final var actual = firstUserService.saveFirstTransactionWithMandatory();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithMandatory");
        // MANDATORY 옵션: 트랜잭션이 있으면 합류, 없으면 예외 발생

        // (1) first에 트랜잭션 X, second에 트랜잭션 O (MANDATORY)
        // 예외 발생, 트랜잭션 not active

        // (2) fisrt에 트랜잭션 O, second에 트랜잭션 O (MANDATORY)
        // second가 first 트랜잭션에 참여하면서, 트랜잭션명이 first로 나타나고 트랜잭션이 active함.
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
        assertThat(actual)
                .hasSize(2)
                .containsExactly(
                        "transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported",
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported"
                );

        // Physical Transactions: 실제 JDBC 상에서의 트랜잭션
        // Logical Transactions: @Transaction 어노테이션을 작성한 메서드 내의 트랜잭션

        // first가 트랜잭션을 사용한다면 1개의 물리적 트랜잭션이 동작
        // first가 트랜잭션을 사용하지 않는다면, second 역시 트랜잭션을 사용하지 않는다는 옵션을 걸었기 때문에, 0개의 물리적 트랜잭션이 동작

        // NOT_SUPPORTED 옵션: 트랜잭션을 전혀 사용하지 않는다.

        // (1) first에 트랜잭션 X, second에 트랜잭션 O (NOT_SUPPORTED)
        // 트랜잭션을 사용하지 않음.

        // (2) fisrt에 트랜잭션 O, second에 트랜잭션 O (NOT_SUPPORTED)
        // second는 first 트랜잭션에 참여하지 않음. first 트랜잭션만 active함.
    }

    /**
     * 아래 테스트는 왜 실패할까?
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     */
    @Test
    void testNested() {
        final var actual = firstUserService.saveFirstTransactionWithNested();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNested");

        // NESTED 옵션: 트랜잭션이 있으면 중첩 트랜잭션을 만든다. 트랜잭션이 없으면 REQUIRED와 동일하게 동작한다.
        // 중첩 트랜잭션은 부모 트랜잭션의 커밋과 롤백에 영향을 받는다.
        // 하지만 중첩 트랜잭션의 커밋과 롤백이 외부에 영향을 주지는 않는다.

        // (1) first에 트랜잭션 X, second에 트랜잭션 O (NESTED)
        // second의 트랜잭션만 동작한다.

        // (2) fisrt에 트랜잭션 O, second에 트랜잭션 O (NESTED)
        // 예외가 발생한다.
        // 이유: 중첩 트랜잭션은 외부에서 롤백이 발생하면 내부에서도 롤백시켜야 한다.
        // 이 기능은 savepoint라는 것으로 구현되어 있는데, JPA는 savepoint를 지원하지 않는다.
    }

    /**
     * 마찬가지로 @Transactional을 주석처리하면서 관찰해보자.
     */
    @Test
    void testNever() {
        final var actual = firstUserService.saveFirstTransactionWithNever();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNever");

        // NEVER 옵션: 트랜잭션을 절대로 사용하지 않는다. 트랜잭션이 있으면 예외가 발생한다.

        // (1) first에 트랜잭션 X, second에 트랜잭션 O (NEVER)
        // 트랜잭션 없이 동작한다.

        // (2) fisrt에 트랜잭션 O, second에 트랜잭션 O (NEVER)
        // 예외가 발생한다.
    }
}
