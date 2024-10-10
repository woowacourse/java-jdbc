package transaction.stage2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
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
     * [생성된 트랜잭션이 몇 개인가?]
     * 1개
     * <p>
     * [왜 그런 결과가 나왔을까?]
     * firstUserService에서 시작된 트랜잭션이 있으면,
     * secondUserService에서 새로운 트랜잭션을 만들지 않고 기존 트랜잭션을 재사용한다.
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
     * [생성된 트랜잭션이 몇 개인가?]
     * 2개
     * <p>
     * [왜 그런 결과가 나왔을까?]
     * 첫 번째 트랜잭션은 REQUIRED 전파 방식으로 트랜잭션을 생성한다.
     * 두 번째 트랜잭션은 REQUIRES_NEW 전파 방식으로 기존 트랜잭션과는 독립된 새로운 트랜잭션을 생성한다.
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
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는지 확인해보자.
     * <p>
     * 1. 첫 번째 트랜잭션에서 새로운 데이터를 저장한다.
     * 2. 두 번째 트랜잭션에서 새로운 트랜잭션을 시작하고 데이터를 저장한다.
     * 3. 첫 번재 트랜잭션에서 예외가 발생하여 롤백된다.
     * 4. 두 번째 트랜잭션은 독립적이기 때문에 예외에 영향을 받지 않고 정상적으로 커밋된다.
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
     * [SUPPORTS 전파 방식]
     * - 트랜잭션이 이미 존재하는 경우 그 트랜잭션에 참여한다.
     * - 트랜잭션이 없으면 트랜잭션 없이 메서드를 실행한다.
     * <p>
     * <p>
     * [주석인 상태 - @Transactional이 없는 경우]
     * saveFirstTransactionWithSupports()는 트랜잭션 없이 실행된다.
     * 따라서 getCurrentTransactionName()은 null을 리턴한다.
     * <p>
     * saveSecondTransactionWithSupports()도 트랜잭션 없이 실행된다.
     * 마찬가지로 getCurrentTransactionName()이 null을 리턴할 것을 예상했지만,
     * 두 번째 트랜잭션의 이름을 리턴한다. - 이유 잘 모름. 트랜잭션이 활성화되지 않더라도 트랜잭션 이름을 기록할 수 있는 스프링의 트랜잭션 관리 메커니즘때문(?)
     * <p>
     * <p>
     * [주석 해제 상태 - @Transactional(propagation = Propagation.REQUIRED)이 있는 경우]
     * saveFirstTransactionWithSupports()가 실행될 때 첫 번째 트랜잭션이 생성된다.
     * 따라서 getCurrentTransactionName()은 첫 번째 트랜잭션의 이름을 리턴한다.
     * <p>
     * saveSecondTransactionWithSupports()는 첫 번째 트랜잭션에 참여한다.
     * 그래서 getCurrentTransactionName()이 첫 번째 트랜잭션의 이름을 리턴한다.
     */
    @Test
    void testSupports() {
        final var actual = firstUserService.saveFirstTransactionWithSupports();

        log.info("transactions : {}", actual);
        // 주석 처리 상태 - @Transactional이 없는 경우
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithSupports");

        // 주석 해제 상태 - @Transactional(propagation = Propagation.REQUIRED)이 있는 경우
//        assertThat(actual)
//                .hasSize(1)
//                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithSupports");
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     * <p>
     * <p>
     * [MANDATORY 전파 방식]
     * - 트랜잭션이 이미 존재하는 경우 그 트랜잭션에 참여한다.
     * - 트랜잭션이 없으면 예외를 발생한다.
     * <p>
     * <p>
     * [주석인 상태 - @Transactional이 없는 경우]
     * saveFirstTransactionWithMandatory()는 트랜잭션 없이 실행된다.
     * 따라서 getCurrentTransactionName()은 null을 리턴한다.
     * <p>
     * 트랜잭션이 없는 상태이므로 saveSecondTransactionWithMandatory() 호출 시 예외가 발생한다.
     * <p>
     * <p>
     * [주석 해제 상태 - @Transactional(propagation = Propagation.MANDATORY)이 있는 경우]
     * saveFirstTransactionWithMandatory()가 실행될 때 첫 번째 트랜잭션이 생성된다.
     * 따라서 getCurrentTransactionName()은 첫 번째 트랜잭션의 이름을 리턴한다.
     * <p>
     * saveSecondTransactionWithMandatory()는 첫 번째 트랜잭션에 참여한다.
     * 그래서 getCurrentTransactionName()이 첫 번째 트랜잭션의 이름을 리턴한다.
     */
    @Test
    void testMandatory() {
        // 주석인 상태 - @Transactional이 없는 경우
//        assertThatThrownBy(() -> firstUserService.saveFirstTransactionWithMandatory())
//                .isInstanceOf(IllegalTransactionStateException.class);

        // 주석 해제 상태 - @Transactional(propagation = Propagation.MANDATORY)이 있는 경우
        final Set<String> actual = firstUserService.saveFirstTransactionWithMandatory();

        log.info("transactions : {}", actual);

        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithMandatory");
    }

    /**
     * [아래 테스트는 몇 개의 물리적 트랜잭션이 동작할까?]
     * 1개
     * <p>
     * FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석 처리하자.
     * [다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이 동작할까?]
     * 0개
     * <p>
     * [물리적 트랜잭션과 논리적 트랜잭션의 차이점]
     * - 물리적 트랜잭션) 데이터베이스 수준에서 발생
     * - 논리적 트랜잭션) 스프링에서 관리하는 트랜잭션의 경계
     * <p>
     * [NOT_SUPPORTED 전파 방식]
     * 기존 트랜잭션이 있으면 일시 중지 후 트랜잭션 없이 로직을 수행한다.
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
     * [아래 테스트는 왜 실패할까?]
     * https://techblog.woowahan.com/2606/
     * "org.springframework.transaction.NestedTransactionNotSupportedException:
     * JpaDialect does not support savepoints - check your JPA provider's capabilities"
     * -> JPA에서 savepoints를 지원하지 않기 때문이다.
     * 세이브포인트 롤백이 발생하면, 데이터베이스의 상태는 롤백되지만, JPA의 영속성 컨텍스트는 롤백되지 않고 변경된 상태를 유지한다.
     * 이로 인해 데이터베이스와 영속성 컨텍스트 사이의 상태 불일치가 발생하며, 커밋 시점에 잘못된 데이터가 데이터베이스에 반영될 수 있다.
     * <p>
     * [FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?]
     * saveSecondTransactionWithNested()에서 새로운 트랜잭션이 생성된다.
     * <p>
     * [NESTED 전파 방식]
     * 기존 트랜잭션이 있으면 save point를 표시하여 예외 발생 시 save point로 롤백한다.
     * 기존 트랜잭션이 없으면 REQUIRED처럼 새로운 트랜잭션을 시작한다.
     */
    @Test
    void testNested() {
        // FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리한 경우
        final Set<String> actual = firstUserService.saveFirstTransactionWithNested();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNested");
    }

    /**
     * 마찬가지로 @Transactional을 주석처리하면서 관찰해보자.
     * <p>
     * [NEVER 전파 방식]
     * 트랜잭션이 없는 상태에서만 실행되며, 기존 트랜잭션이 있으면 예외를 발생한다.
     * 트랜잭션이 절대 있어서는 안 되는 상황에서 사용한다.
     */
    @Test
    void testNever() {
        // 주석 해제한 경우
//        assertThatThrownBy(() -> firstUserService.saveFirstTransactionWithNever())
//                .isInstanceOf(IllegalTransactionStateException.class);

        // 주석 처리한 경우
        final Set<String> actual = firstUserService.saveFirstTransactionWithNever();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNever");
    }
}
