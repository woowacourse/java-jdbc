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
     * 생성된 트랜잭션이 몇 개인가? : 1개
     * 왜 그런 결과가 나왔을까? : 내부에서 secondUserService.saveSecondTransactionWithNotSupported를 전파 옵션이 REQUIRED라서
     * 이미 생성된 트렌젝션이 있을 경우 해당 트렌젝션(firstUserService.saveFirstTransactionWithRequired())을 사용해서
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
     * 왜 그런 결과가 나왔을까? SecondUserService.saveSecondTransactionWithRequiresNew 의 전파 옵션이 REQUIRED_NEW라서
     * 이미 있는 트렌젝션과 관계없이 트렌젝션 시작 시 새로운 트렌젝션을 생성함.
     */
    @Test
    void testRequiredNew() {
        final var actual = firstUserService.saveFirstTransactionWithRequiredNew();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew",
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew");
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는 지 확인해보자.
     * firstUserService에서 추가한 사용자는 롤백되었고, secondUserService에서 추가한 서비스는 REQUIRED_NEW이기 때문에 사용자가 추가되었다.
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
     * 주석인 상태 : 호출한 메서드에 트렌젝션이 없을 경우 새로운 트렌젝션을 생성한다.
     * 주석 해제 한 상태 : 호출한 메서드에 트렌젝션이 있으면 REQUIRED 처럼 동작한다.
     */
    @Test
    void testSupports() {
        final var actual = firstUserService.saveFirstTransactionWithSupports();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithSupports");
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * 주석인 상태 : 호출자에 트렌젝션이 존재하지 않을 경우 새로운 트렌젝션을 생성하지 않는다.
     * 주석 해제 한 상태 : 호출자에 트렌젝션이 존재할 경우 기존 트렌젝션을 사용한다.
     * SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     * Mandatory의 뜻 : required by law or rules; compulsory. (명령의)
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
     * 주석 처리를 하지 않을 때 : 2개의 트렌젝션이 생성되지만, 아래 처럼 SecondUserService의 트렌젝션은 활성화하지 않는다.
     * 2024-10-09T15:02:19.039+09:00  INFO 814749 --- [    Test worker] transaction.stage2.FirstUserService      :
     * transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported is Actual Transaction Active : ✅ true
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-09T15:02:19.051+09:00  INFO 814749 --- [    Test worker] transaction.stage2.SecondUserService     :
     * transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported is Actual Transaction Active : ❌ false
     *
     * 주석 처리를 할 때: 1개의 트렌젝션이 생성되지만 SecondUserService의 트렌젝션은 활성화하지 않는다.
     * 2024-10-09T15:04:24.396+09:00  INFO 815227 --- [    Test worker] transaction.stage2.FirstUserService      :
     * null is Actual Transaction Active : ❌ false
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-09T15:04:24.400+09:00  INFO 815227 --- [    Test worker] transaction.stage2.SecondUserService     :
     * transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported is Actual Transaction Active : ❌ false
     *
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     * https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-propagation.html
     * 물리적 트렌젝션 : 실제 db와 연결되어 있는 트렌젝션 - Actual Transaction Active와 관련되어있는 것 같다.
     * 논리적 트렌젝션 : 메서드 별에 @transactional 어노테이션으로 나눈 트렌젝션. 트렌젝션인 척 하는 트렌젝션.
     * Actual Transaction Active가 false이므로 트렌젝션이 붙여져 있어도 롤백이 되지 않을 것이다.(실험 했더니 그렇더라. 왜 있는 옵션일까?)
     */
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported",
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported");
    }

    /**
     * 아래 테스트는 왜 실패할까? Hibernate JPA는 savepoint를 사용할 수 없음
     * 주석 처리 전 : org.springframework.transaction.NestedTransactionNotSupportedException: JpaDialect does not support savepoints - check your JPA provider's capabilities
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     * 주석 처리 후 : 된다! 기존 트렌젝션이 존재하지 않아서 새 물리적 트렌젝션을 생성하는 것으로 보인다.
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
     * 주석 처리 전 : 예외 발생 "Existing transaction found for transaction marked with propagation 'never'"
     * 주석 처리 후 : 새로운 "논리적" 트렌젝션을 생성한다.
     * 결론 : 전파받는 것을 허용하지 않는 옵션으로 보인다. 호출자에 트렌젝션이 존재할 경우 예외를 발생시킨다.
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
