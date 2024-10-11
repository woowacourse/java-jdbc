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
     * 트랜잭션 전파 속성이 Required
     * 두 개의 논리 트랜잭션을 묶어서 하나의 물리적 트랜잭션을 사용
     * 내부 트랜잭션이 기존에 존재하는 외부 트랜잭션에 참여함
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
     * 외부 트랜잭션과 내부 트랜잭션 완전히 분리 -> 물리 트랜잭션 2개
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
     * Hibernate: select u1_0.id,u1_0.account,u1_0.email,u1_0.password from users u1_0
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-09T21:21:26.195+09:00  INFO 34536 --- [    Test worker] transaction.stage2.SecondUserService     :
     * transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew is Actual Transaction Active : ✅ true
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-09T21:21:26.198+09:00  INFO 34536 --- [    Test worker] transaction.stage2.FirstUserService      :
     * transaction.stage2.FirstUserService.saveAndExceptionWithRequiredNew is Actual Transaction Active : ✅ true
     * Hibernate: select u1_0.id,u1_0.account,u1_0.email,u1_0.password from users u1_0
     * Hibernate: select u1_0.id,u1_0.account,u1_0.email,u1_0.password from users u1_0
     * Hibernate: delete from users where id=?
     *
     * REQUIRES_NEW이면 새로운 물리적 트랜잭션 생성
     * -> 외부 트랜잭션(FirstUserService)의 롤백과 상관 없이 내부 트랜잭션(SecondUserService)은 롤백되지 않는다.
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
     * SecondUserService -> SUPPORTS
     * -> 기존 트랜잭션이 있으면 참여하고 없으면 트랜잭션 없이 진행한다.
     * 주석인 상태에서 실행 시 트랜잭션 없이 진행한다.
     * 주석을 해제하고 실행 시 기존 트랜잭션에 참여한다
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
     * MANDATORY는 트랜잭션이 필수 -> 기존 트랜잭션이 없으면 `IllegalTransactionStateException` 예외가 발생한다.
     * 트랜잭션이 있으면 기존 트랜잭션에 참여한다.
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
     * NOT_SUPPORTED -> 트랜잭션 없이 진행
     * ✅ 주석 있는 경우 (트랜잭션 O)
     * 2024-10-09T21:36:39.668+09:00  INFO 35990 --- [    Test worker] transaction.stage2.FirstUserService      :
     * transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported is Actual Transaction Active : ✅ true
     * 2024-10-09T21:36:39.672+09:00  INFO 35990 --- [    Test worker] transaction.stage2.SecondUserService     :
     * transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported is Actual Transaction Active : ❌ false
     * 기존 트랜잭션 보류 시키고 진행
     *
     * ✅ 주석 없는 경우 (트랜잭션 X)
     *2024-10-09T21:38:25.784+09:00  INFO 36164 --- [    Test worker] transaction.stage2.FirstUserService      :
     * null is Actual Transaction Active : ❌ false
     * 2024-10-09T21:38:25.786+09:00  INFO 36164 --- [    Test worker] transaction.stage2.SecondUserService     :
     * transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported is Actual Transaction Active : ❌ false
     * 트랜잭션 없이 진행
     *
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     */
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported");
    }

    /**
     * 아래 테스트는 왜 실패할까?
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     *
     * NESTED : 중첩 트랜잭션 생성
     * -> 자식 트랜잭션의 롤백이 부모 트랜잭션에 영향을 미치지 않지만, 부모 트랜잭션의 롤백 -> 자식 트랜잭션에 영향 미침
     *
     * ✅ 주석 없는 경우 (트랜잭션 O)
     * 2024-10-09T21:46:59.229+09:00  INFO 36895 --- [    Test worker] transaction.stage2.FirstUserService      :
     * transaction.stage2.FirstUserService.saveFirstTransactionWithNested is Actual Transaction Active : ✅ true
     * JpaDialect does not support savepoints - check your JPA provider's capabilities
     * -> NESTED는 JDBC의 savepoint 기능을 사용 -> JPA에서 사용 불가
     *
     * ✅ 주석 있는 경우 (트랜잭션 X)
     * 2024-10-09T21:51:46.719+09:00  INFO 37428 --- [    Test worker] transaction.stage2.FirstUserService      :
     * null is Actual Transaction Active : ❌ false
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-09T21:51:46.721+09:00  INFO 37428 --- [    Test worker] transaction.stage2.SecondUserService     :
     * transaction.stage2.SecondUserService.saveSecondTransactionWithNested is Actual Transaction Active : ✅ true
     * -> 트랜잭션이 없으면 새로운 트랜잭션을 만듦
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
     * NEVER -> 트랜잭션 사용하지 않음 -> 기존 트랜잭션도 허용 X
     *
     * ✅주석 없는 경우 (트랜잭션 O)
     * IllegalTransactionStateException 예외 발생
     *
     * ✅주석 있는 경우 (트랜잭션 X)
     *2024-10-09T21:55:07.300+09:00  INFO 37773 --- [    Test worker] transaction.stage2.FirstUserService      :
     * null is Actual Transaction Active : ❌ false
     * 2024-10-09T21:55:07.302+09:00  INFO 37773 --- [    Test worker] transaction.stage2.SecondUserService     :
     * transaction.stage2.SecondUserService.saveSecondTransactionWithNever is Actual Transaction Active : ❌ false
     * -> 트랜잭션 하나도 없음
     *
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
