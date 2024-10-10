package transaction.stage2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.IllegalTransactionStateException;

/**
 * 트랜잭션 전파(Transaction Propagation)란? 트랜잭션의 경계에서 이미 진행 중인 트랜잭션이 있을 때 또는 없을 때 어떻게 동작할 것인가를 결정하는 방식을 말한다.
 * <p>
 * FirstUserService 클래스의 메서드를 실행할 때 첫 번째 트랜잭션이 생성된다. SecondUserService 클래스의 메서드를 실행할 때 두 번째 트랜잭션이 어떻게 되는지 관찰해보자.
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
     * 생성된 트랜잭션이 몇 개인가? 왜 그런 결과가 나왔을까?
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
     * 생성된 트랜잭션이 몇 개인가? 왜 그런 결과가 나왔을까?
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
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다. REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이
     * 발생하는지 확인해보자.
     */
    @Test
    void testRequiredNewWithRollback() {
        assertThat(firstUserService.findAll()).hasSize(0);

        assertThatThrownBy(() -> firstUserService.saveAndExceptionWithRequiredNew())
                .isInstanceOf(RuntimeException.class);

        assertThat(firstUserService.findAll()).hasSize(1);
    }

    /**
     * FirstUserService.saveFirstTransactionWithSupports() 메서드를 보면 @Transactional이 주석으로 되어 있다. 주석인 상태에서 테스트를 실행했을 때와 주석을
     * 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * <p>
     * 1. 주석 상태 service1은 트랜잭션 설정이 안되었고, service2는 트랜잭션 전파 범위가 Propagation.SUPPORTS로 설정 Propagation.SUPPORTS은 외부 트랜잭션이
     * 없으면 트랜잭션 없이 실행 service2는 트랜잭션이 없는 상태로 실행 물리 트랜잭션 2개 사용
     * <p>
     * 2. 주석을 지운 상태 service1은 트랜잭션 전파 범위가 Propagation.REQUIRED로 설정이 되어 있고, service2는 트랜잭션 전파 범위가 Propagation.SUPPORTS로
     * 설정 Propagation.SUPPORTS은 외부 트랜잭션이 있으면 외부 트랜잭션을 사용 service2는 트랜잭션이 있는 상태로 실행 물리 트랜잭션은 1개 사용
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
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다. 주석인 상태에서 테스트를 실행했을 때와
     * 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자. SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     */
    @Test
    void testMandatory() {
        assertThatThrownBy(() -> firstUserService.saveFirstTransactionWithMandatory())
                .isInstanceOf(IllegalTransactionStateException.class);
    }

    /**
     * 아래 테스트는 몇 개의 물리적 트랜잭션이 동작할까?
     * <p>
     * + FirstUserService.saveFirstTransactionWithNotSupported()에서 물리적 트랜잭션 동작
     * + SecondUserService의 SimpleJpaRepository.save()에서 물리적 트랜잭션 동작
     * + teardown에서 물리적 트랜잭션 동작
     * <p>
     * FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석 처리하자. 다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이
     * 동작할까?
     * <p>
     * + FirstUserService의 SimpleJpaRepository.save()에서 물리적 트랜잭션 동작
     * + SecondUserService의 SimpleJpaRepository.save()에서 물리적 트랜잭션 동작
     * + teardown에서 물리적 트랜잭션 동작
     * <p>
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자. ref.
     * https://docs.spring.io/spring-framework/docs/4.2.x/spring-framework-reference/html/transaction.html#tx-propagation
     * <p>
     * 물리 트랜잭션은 데이터베이스와 같은 실제 리소스에 대해 수행되는 트랜잭션이다. 물리 트랜잭션은 데이터베이스에 실제로 영향을 미치고, 커밋이나 롤백 등의 작업이 포함된다. 또한 데이터의 일관성 및 무결성을
     * 보장한다.
     * <p>
     * 논리 트랜잭션은 애플리케이션의 비즈니스 로직을 기준으로 정의된 트랜잭션이다. 하나의 물리 트랜잭션을 여러 논리 트랜잭션에서 공유할 수 있다.
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
     * 아래 테스트는 왜 실패할까? JPA는 NEST를 지원하지 않는다.
     * https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/orm/jpa/JpaTransactionManager.html
     * <p>
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
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
