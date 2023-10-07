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
     * 생성된 트랜잭션이 몇 개인가? : propagetion이 required면 새로운 트랜잭션을 생성하지 않고 기존의 트랜잭션에 참여하기 때문에 1개의 트랜잭션만 생성된다.
     * 왜 그런 결과가 나왔을까? : 트랜잭션의 이름은 메서드의 이름이 된다.
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
     * 생성된 트랜잭션이 몇 개인가? : propagation이 required_new면 호출할 때마다 새로운 트랜잭션을 생성하기 때문에 해당 테스트에선 2개의 트랜잭션이 생성된다.
     * 왜 그런 결과가 나왔을까? : 트랜잭션의 이름은 메서드의 이름이 되는데, 트랜잭션이 두개니까 이름도 두개가 된다.
     */
    @Test
    void testRequiredNew() {
        final var actual = firstUserService.saveFirstTransactionWithRequiredNew(true);

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew",
                        "transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew");
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는 지 확인해보자.
     * :second 트랜잭션이 예외 발생으로 인해 rollback되면서 first 로직에 예외가 던져지는데
     * 여기서 예외에 대한 처리를 하지 않았기 때문에 rollback된다. 즉, 저장된 user는 없기 때문에 size가 0이다.
     */
    @Test
    void testRequiredNewWithRollback() {
        assertThat(firstUserService.findAll()).hasSize(0);

        assertThatThrownBy(() -> firstUserService.saveAndExceptionWithRequiredNew(false))
                .isInstanceOf(RuntimeException.class);

        assertThat(firstUserService.findAll()).hasSize(0);
    }

    /**
     * FirstUserService.saveFirstTransactionWithSupports() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * 1. 주석이 없는 경우
     *      first에서 트랜잭션을 생성하지 않고 second를 호출함. second에선 @Transactional의 속성으로 supports를 지정했는데
     *      이는 이미 존재하는 트랜잭션이 있으면 참여하고 없으면 트랜잭션 없이 실행한다는 의미이다.
     *      트랜잭션 없이 실행하지만 @Transactional 어노테이션이 붙은 이상 "논리 트랜잭션"이 만들어져 이름도 메서드로 지정되지만
     *      그 트랜잭션은 "물리 트랜잭션"이 아니라 활성화되진 않는다.
     * 2. 주석이 있는 경우
     *      first에서 트랜잭션을 생성하고 second를 호출함. second에선 @Transactional의 속성으로 supports를 지정했기 때문에
     *      first에서 생성한 트랜잭션에 참여하게 된다. 따라서 first에서 생성한 트랜잭션의 이름이 second에서도 그대로 사용된다.
     *
     */
    @Test
    void testSupports() {
        final var actual = firstUserService.saveFirstTransactionWithSupports();

        log.info("transactions : {}", actual);
        // 주석이 없는 경우
//        assertThat(actual)
//                .hasSize(1)
//                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithSupports");

        // 주석이 있는 경우
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.FirstUserService.saveFirstTransactionWithSupports");
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     * 1. 주석이 없을 때
     *      first에서 트랜잭션을 생성하지 않고 second를 호출함. second에선 @Transactional의 속성으로 mandatory를 지정했는데
     *      이는 이미 존재하는 트랜잭션이 있으면 참여하고 없으면 예외를 발생시킨다는 의미이다.
     *      예외가 발생했기 때문에 first에서도 예외가 발생하고 테스트가 실패한다.
     * 2. 주석이 있을 때
     *      first에서 트랜잭션을 생성하고 second를 호출함. second에선 @Transactional의 속성으로 mandatory를 지정했기 때문에
     *      first에서 생성한 트랜잭션에 참여하게 된다. 따라서 first에서 생성한 트랜잭션의 이름이 second에서도 그대로 사용된다.
     */
    @Test
    void testMandatory() {
        // 주석이 없을 때
//        assertThatThrownBy(() -> firstUserService.saveFirstTransactionWithMandatory())
//                .isInstanceOf(IllegalTransactionStateException.class);

        // 주석이 있을 때
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
     * 1. FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석이 있을 때
     *      first에서 트랜잭션을 생성하고 second를 호출함. second에선 @Transactional의 속성으로 not_supported를 지정했는데
     *      이는 이미 존재하는 트랜잭션이 있으면 일시 중단하고 없으면 트랜잭션 없이 실행한다는 의미이다.
     *      따라서 first에서 생성한 트랜잭션을 일시 중단하고 second에서는 트랜잭션 없이 실행된다.
     *      이때 second에선 @Transactional 어노테이션이 있기 때문에 이 자체로 "논리 트랜잭션"이 생성되지만 "물리 트랜잭션"은 생성되지 않는다.
     * 2. FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석이 없을 때
     *      first에서 트랜잭션없이 second를 호출함. first는 @Transactional 어노테이션이 없어 논리 및 물리 트랜잭션이 생성되지 않는다.
     *      second에선 @Transactional의 속성으로 not_supported를 지정했기 때문에 "논리 트랜잭션"이 생성되지만 "물리 트랜잭션"은 생성되지 않는다.
     *
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     */
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        // FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석이 있을 때
//        assertThat(actual)
//                .hasSize(2)
//                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported",
//                        "transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported");
        // FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석이 없을 때
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported");
    }

    /**
     * 아래 테스트는 왜 실패할까?
     *  중첩 트랜잭션은 JDBC 3.0 이후 버전의 savepoint기능을 사용하는데,
     *  JPA를 사용하는 경우, 변경감지를 통해서 업데이트문을 최대한 지연해서 발행하는 방식을 사용하기 때문에
     *  중첩된 트랜잭션 경계를 설정할 수 없어 지원하지 않는다.
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     *  first에서 트랜잭션 없이 second를 호출함. first는 @Transactional 어노테이션이 없어 논리 및 물리 트랜잭션이 생성되지 않는다.
     *  second에선 @Transactional의 속성으로 nested를 지정했기 때문에 새로운 물리 트랜잭션을 생성한다.
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
     * 1. FirstUserService.saveFirstTransactionWithNever() 메서드의 @Transactional을 주석이 있을 때
     *      first에서 트랜잭션을 시작하고 second를 호출함. second에선 @Transactional의 속성으로 never를 지정했는데
     *      이는 이미 존재하는 트랜잭션이 있으면 예외를 발생시키고 없으면 트랜잭션 없이 실행한다는 의미이다.
     *      따라서 first에서 생성한 트랜잭션이 존재하기 때문에 예외가 발생한다.
     * 2. FirstUserService.saveFirstTransactionWithNever() 메서드의 @Transactional을 주석이 없을 때
     *      first에서 트랜잭션 없이 second를 호출함. second에선 @Transactional의 속성으로 never를 지정했기 때문에
     *      second에선 @Transactional의 속성으로 never를 지정했기 때문에 "논리 트랜잭션"이 생성되지만 "물리 트랜잭션"은 생성되지 않는다.
     */
    @Test
    void testNever() {
        // FirstUserService.saveFirstTransactionWithNever() 메서드의 @Transactional을 주석이 있을 때
//        assertThatThrownBy(() -> firstUserService.saveFirstTransactionWithNever())
//                .isInstanceOf(IllegalTransactionStateException.class);

        // IllegalTransactionStateException
        final var actual = firstUserService.saveFirstTransactionWithNever();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNever");
    }
}
