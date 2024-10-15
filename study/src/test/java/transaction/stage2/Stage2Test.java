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
     * REQUIRED & REQUIRED
     * 생성된 트랜잭션: 1개
     * REQUIRED는 여러 개의 논리 트랜잭션이 하나의 물리 트랜잭션을 공유하기 때문
     */
    @Test
    void testRequired() {
        final var actual = firstUserService.saveFirstTransactionWithRequired();

        log.info("transactions : {}", actual);
        //트랜잭션이 실행됐을 때 각 메서드의 상태(공유-비공유) 개수를 테스트
        //Set<>은 중복을 허용하지 않기 때문에, @Transactional 어노테이션이 붙어있는 메서드의 상위 transaction name과 하위 transaction name이 동일한 경우 개수를 1개로 취급
        //둘다 @Transactional이 붙어있고 위 트랜잭션을 이어받는 경우(합류, 공유) 동일한 이름
        //둘다 @Transactional이 붙어있고 보류하거나 하는 식으로 위 트랜잭션과 다른 상태가 되는 경우 다른 이름
        //하위에만 붙어있는 경우 하위 메서드의 이름
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithRequired");
    }

    /**
     * REQUIRED & REQUIRES_NEW
     * 생성된 트랜잭션: 2개
     * REQUIRES_NEW는 이미 물리 트랜잭션이 존재하더라도 새로운 트랜잭션을 열기 때문
     */
    @Test
    void testRequiredNew() {
        final var actual = firstUserService.saveFirstTransactionWithRequiredNew();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew",
                        "transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew");
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는지 확인해보자.
     * <p>
     * REQUIRES_NEW는 새로운 트랜잭션을 열기 때문에 외부 트랜잭션이 실패해도 변화 내용이 커밋됨
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
     * SUPPORTS
     * 주석인 상태에서 테스트를 실행했을 때(기존 트랜잭션 없음): 새로운 트랜잭션 생성
     * 주석을 해제하고 테스트를 실행했을 때(기존 트랜잭션 있음): 기존 트랜잭션 합류
     */
    @Test
    void testSupports() {
        final var actual = firstUserService.saveFirstTransactionWithSupports();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                //주석인 상태(기존 트랜잭션 없음)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithSupports");
        //주석 해제(기존 트랜잭션 있음)
        //.containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithSupports");
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때(기존 트랜잭션 없음): IllegalTransactionStateException 발생
     * 주석을 해제하고 테스트를 실행했을 때(기존 트랜잭션 있음): 기존 트랜잭션 합류
     * SUPPORTS와 어떤 점이 다른지: 기존 트랜잭션이 없을 때
     * - SUPPORT는 새로운 트랜잭션 생성
     * - MANDATORY는 예외 발생
     */
    @Test
    void testMandatory() {
        //주석인 상태(기존 트랜잭션 없음)
        assertThatThrownBy(() -> firstUserService.saveFirstTransactionWithMandatory())
                .isExactlyInstanceOf(IllegalTransactionStateException.class);

        /*final var actual = firstUserService.saveFirstTransactionWithMandatory();

        log.info("transactions : {}", actual);

        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithMandatory");*/
    }

    /**
     * 로그 켜고 setAutoCommit(false) 개수로 확인
     * 아래 테스트는 2개의 물리적 트랜잭션이 동작: first - @Transactional(REQUIRED), second - save() 메서드(SpringJPA에서 지원)
     * 주석 처리해도 2개의 물리적 트랜잭션이 동작: first, second - save() 메서드
     * <p>
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     */
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported");

        //주석 지웠을 때(기존 트랜잭션 존재)
        //.hasSize(2)
        //.containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported",
        //                        "transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported");
    }

    /**
     * 주석인 상태에서 테스트를 실행했을 때(기존 트랜잭션 없음): 새로운 트랜잭션 생성
     * 주석을 해제하고 테스트를 실행했을 때(기존 트랜잭션 있음):org.springframework.transaction.NestedTransactionNotSupportedException: JpaDialect does not support savepoints - check your JPA provider's capabilities
     * JPA는 savepoint를 지원하지 않음 / Nested 못씀 ㅎㅎ
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
     * 주석인 상태에서 테스트를 실행했을 때(기존 트랜잭션 없음): 트랜잭션 없이 진행
     * 주석을 해제하고 테스트를 실행했을 때(기존 트랜잭션 있음): IllegalTransactionStateException 발생
     */
    @Test
    void testNever() {
        //주석 없을 때
        /*assertThatThrownBy(() -> firstUserService.saveFirstTransactionWithNever())
                .isExactlyInstanceOf(IllegalTransactionStateException.class);*/

        //주석 있을 때
        final var actual = firstUserService.saveFirstTransactionWithNever();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNever");
    }
}
