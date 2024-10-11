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
    public FirstUserService firstUserService;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    /**
     * 생성된 트랜잭션이 몇 개인가? 1개
     * 왜 그런 결과가 나왔을까?
     * PROPAGATION_REQUIRED 는 트랜잭션이 없다면 새로운 트랜잭션을 생성하고,
     * 이미 트랜잭션이 존재한다면 해당 트랜잭션에 참여한다.
     *
     * note:
     * 기본적으로 참여하는 트랜잭션은 로컬 격리 수준, 시간 초과 값 또는 읽기 전용 플래그(있는 경우)를 무시하고 외부 범위의 특성에 합류합니다.
     * 라고 함
     * 아마 외부 트랜잭션에 참여할 땐 지금 메서드의 설정값들을 무시한다는 이야기인 것 같음
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
     * 생성된 트랜잭션이 몇 개인가? 2
     * 왜 그런 결과가 나왔을까?
     * PROPAGATION_REQUIRES_NEW 는 항상 독립적인 트랜잭션을 생성
     *  절대 외부 트랜잭션에 참여하지 않음
     */
    @Test
    void testRequiredNew() {
        final var actual = firstUserService.saveFirstTransactionWithRequiredNew();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        "transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew",
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew"
                )
        ;
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는 지 확인해보자.
     *
     * -> REQUIRES_NEW 는 물리적 트랜잭션을 새로 만든다.
     *  second 의 트랜잭션이 커밋된 이후 first 의 트랜잭션이 롤백 되기 때문에 second 의 트랜잭션을 롤백시키지 않는다.
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
     * 주석: 물리적 트랜잭션 0개
     *
     * 주석 해제: 물리적 트랜잭션 1개
     *
     * Supports 는 트랜잭션이 있으면 해당 트랜잭션을 이용 / 트랜잭션이 없으면 비트랜잭션으로 실행
     */
    @Test
    void testSupports() {
        final var actual = firstUserService.saveFirstTransactionWithSupports();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly(
//                        "transaction.stage2.FirstUserService.saveFirstTransactionWithSupports"
                        "transaction.stage2.SecondUserService.saveSecondTransactionWithSupports"
                );
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     *
     * 주석: 예외!!
     *
     * 주석 해제: 물리적 트랜잭션 1개
     *
     * Supports 는 트랜잭션이 있으면 해당 트랜잭션을 이용 / 트랜잭션이 없으면 예외 발생
     */
    @Test
    void testMandatory() {
        final var actual = firstUserService.saveFirstTransactionWithMandatory();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly(
//                        "transaction.stage2.SecondUserService.saveSecondTransactionWithMandatory"
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithMandatory"
                );
    }

    /**
     * 아래 테스트는 몇 개의 물리적 트랜잭션이 동작할까?
     * FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석 처리하자.
     * 다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이 동작할까?
     *
     * 주석: 0
     *
     * 주석 해제: 1
     *
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     * -> 논리적 트랜잭션은 Spring 에서 지원하는 하나의 Transaction 범위
     * -> 물리적 트랜잭션은 실제 DB 에서 지원하는 Transaction 범위
     *
     * NOT_SUPPORTED 는 먼저 존재하는 트랜잭션이 있다면 일시 중단한 다음 트랜잭션 없이 비즈니스 로직이 실행된다.
     */
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported",
                        "transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported"
                );
    }

    /**
     * 아래 테스트는 왜 실패할까?
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     *
     * NESTED 는 기존 트랜잭션이 있다면 저장 지점을 표시한다.
     *  예외가가 발생하면 해당 저장 지점으로 롤백된다.
     *  기존 트랜잭션이 있었지만, 저장 지점이 없어서 문제가 되었다.
     * 기존 트랜잭션이 없으면 REQUIRED 처럼 동작한다.
     * https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-propagation
     * .html#tx-propagation-nested
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
     * -> Never 은 활성 트랜잭션이 있으면 예외를 던진다.
     */
    @Test
    void testNever() {
        final var actual = firstUserService.saveFirstTransactionWithNever();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly(
                        "transaction.stage2.SecondUserService.saveSecondTransactionWithNever"
                );
    }
}
