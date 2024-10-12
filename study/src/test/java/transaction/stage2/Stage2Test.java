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
     * ---
     * 생성된 트랜잭션의 수 : 1개
     * 원인 : 트랜잭션 전파 설정이 모두 required여서, 같은 트랜잭션을 사용한다.
     */
    @Test
    void testRequired() {
        final var actual = firstUserService.saveFirstTransactionWithRequired();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly(actual.toArray(new String[0]));
    }

    /**
     * 생성된 트랜잭션이 몇 개인가?
     * 왜 그런 결과가 나왔을까?
     * ---
     * 내부 트랜잭션(second transaction)의 속성이 required_new
     * 외부 트랜잭션과 별개로 트랜잭션을 만든다.
     */
    @Test
    void testRequiredNew() {
        final var actual = firstUserService.saveFirstTransactionWithRequiredNew();

        String[] result = actual.toArray(new String[0]);
        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly(result[0], result[1]);
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는 지 확인해보자.
     * ---
     * 내부 트랜잭션의 속성이 required_new로 설정되어 있기 때문에 새 커넥션을 획득하여 트랜잭션을 시작한다.
     * 그렇기 때문에 롤백되어도 외부 트랜잭션에 영향을 주지 않는다.
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
     * ---
     * 내부 트랜잭션의 속성은 support로 설정되어 있다.
     * support는 외부 트랜잭션이 존재하면 참여하고, 없으면 트랜잭션 없이 진행한다.
     * 주석인 상태는 외부 트랜잭션이 존재하지 않는다. 이 경우 내부 트랜잭션은 시작되지 않는다.
     * 주석을 해제하면 외부 트랜잭션에 내부 트랜잭션이 참여한다.
     */
    @Test
    void testSupports() {
        final var actual = firstUserService.saveFirstTransactionWithSupports();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly(actual.toArray(new String[0]));
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     * ---
     * mandatory로 설정 시 외부 트랜잭션은 반드시 존재해야 한다. 없으면 IllegalTransactionStateException이 발생한다.
     * support는 외부 트랜잭션이 필수로 필요하진 않지만 mandatory는 반드시 필요하다.
     * 두 설정 모두 트랜잭션이 존재하면 참여한다.
     */
    @Test
    void testMandatory() {
        final var actual = firstUserService.saveFirstTransactionWithMandatory();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly(actual.toArray(new String[0]));
    }

    /**
     * 아래 테스트는 몇 개의 물리적 트랜잭션이 동작할까?
     * FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석 처리하자.
     * 다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이 동작할까?
     *
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     * ---
     * @Transactional을 주석 처리하면 0개의 물리적 트랜잭션이 동작한다.
     * 외부 트랜잭션이 존재하지 않고, 내부 트랜잭션의 설정은 not_supported로 트랜잭션을 지원하고 있지 않다.
     *
     * 물리적 트랜잭션 : 실제로 커넥션을 얻는 트랜잭션
     * 논리적 트랜잭션 : 스프링의 트랜잭션 매니저로 처리하는 트랜잭션
     */
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly(actual.toArray(new String[0]));
    }

    /**
     * 아래 테스트는 왜 실패할까?
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     * ---
     * secondTransaction의 설정인 nested는 중첩 트랜잭션을 사용하여 부모 트랜잭션에 자식 트랜잭션을 생성한다.
     * 부모 트랜잭션의 결과는 자식 트랜잭션에 영향을 준다. 반면 자식 트랜잭션은 부모 트랜잭션에 영향을 주지 않는다.
     * 이때 nested 설정에서 사용하는 JDBC의 savepoint 기능은 JPA에서 지원하지 않기 때문에 에러가 발생한다.
     * ---
     * 외부 트랜잭션이 존재하지 않으면 자식 트랜잭션을 생성하는 것이 아니라, 새 트랜잭션을 생성하기 때문에 에러가 발생하지 않는다.
     */
    @Test
    void testNested() {
        final var actual = firstUserService.saveFirstTransactionWithNested();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly(actual.toArray(new String[0]));
    }

    /**
     * 마찬가지로 @Transactional을 주석처리하면서 관찰해보자.
     * ---
     * secondTransaction의 설정인 never는 트랜잭션을 적용하지 않는 설정이다.
     * 외부 트랜잭션이 존재하면 IllegalTransactionStateException이 발생한다.
     * 외부 트랜잭션이 없으면 트랜잭션을 지원하지 않는다.
     */
    @Test
    void testNever() {
        final var actual = firstUserService.saveFirstTransactionWithNever();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly(actual.toArray(new String[0]));
    }
}
