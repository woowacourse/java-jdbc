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
 *
 * FirstUserService 클래스의 메서드를 실행할 때 첫 번째 트랜잭션이 생성된다.
 * SecondUserService 클래스의 메서드를 실행할 때 두 번째 트랜잭션이 어떻게 되는지 관찰해보자.
 *
 * 스프링에서는 데이터베이스가 제공하는 트랜잭션을 물리 트랜잭션, 자체적으로 관리하는 트랜잭션을 논리 트랜잭션이라 한다.
 * 물리 트랜잭션에는 여러 논리 트랜잭션이 매핑될 수 있다.
 * 물리 트랜잭션은 매핑된 모든 논리 트랜잭션이 커밋되어야만 커밋된다.
 * 논리트랜잭션이 하나라도 롤백되면 물리 트랜잭션이 롤백된다.
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
     * 생성된 트랜잭션이 몇 개인가? 물리 1개 논리 2개
     * 왜 그런 결과가 나왔을까?
     * 내부에서 수행되는 saveSecondTransactionWithRequired 메서드에는 @Transactional(propagation = Propagation.REQUIRED) 옵션이 붙어있다.
     * 이 옵션은 현재 스코프 혹은 상위 스코프에 물리 트랜잭션이 없는 경우 새 물리 트랜잭션을 시작하고, 있는 경우 그 물리 트랜잭션에 참여하는 옵션이다.
     * 상위 스코프 트랜잭션의 격리레벨, 타임아웃 설정, 읽기전용 플래그를 따라간다.
     * saveFirstTransactionWithRequired 메서드에 의해 트랜잭션이 시작되므로 saveSecondTransactionWithRequired 은 이 트랜잭션에 참여한다.
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
     * 생성된 트랜잭션이 몇 개인가? 물리 2개 논리 2개
     * 왜 그런 결과가 나왔을까?
     * 내부에서 수행되는 saveSecondTransactionWithRequiresNew 메서드에는 @Transactional(propagation = Propagation.REQUIRES_NEW) 옵션이 붙어있다.
     * 이 옵션은 상위 스코프에 적용중인 물리 트랜잭션이 있어도 이에 참여하지 않고 독자적인 물리 트랜잭션을 생성하는 옵션이다.
     * 상위 스코프 트랜잭션의 격리레벨, 타임아웃 설정, 읽기전용 플래그를 따라가지 않는다. 독자적으로 이를 설정할 수 있다.
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
                );
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는 지 확인해보자.
     * saveSecondTransactionWithRequiresNew 메서드에 적용된 @Transactional(propagation = Propagation.REQUIRES_NEW) 옵션은 상위 스코프의 트랜잭션과 무관한 트랜잭션을 생성하므로
     * 상위 스코프 트랜잭션이 롤백되어도 saveSecondTransactionWithRequiresNew 에 의해 수정된 사항은 롤백되지 않는다.
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
     * 주석인 경우 FirstUserService.saveFirstTransactionWithSupports() 내부에서 호출한 SecondUserService.saveSecondTransactionWithSupports() 에 의해 트랜잭션이 생성된다.
     * 그리고 그 트랜잭션은 활성화되지 않는다. 즉, 트랜잭션이 적용되지 않은 상태로 실행된다. => 물리 1개 논리 1개
     * 반면, 주석이 아닌 경우 FirstUserService.saveFirstTransactionWithSupports()에 의해 트랜잭션이 생성되고 이것이 적용된다. => 물리 1개 논리 2개
     *
     * 즉, 상위 스코프에 활성화된 트랜잭션이 존재하는 경우 이에 참여하고, 없는 경우 비활성화된 트랜잭션이 생성되고 트랜잭션이 활성화되지 않은 상태로 실행된다.
     * 상위 스코프에 활성화된 트랜잭션이 없는 경우, 트랜잭션이 활성화되지 않으므로 예외가 발생해도 롤백되지 않는다.
     */
    @Test
    void testSupports() {
        Set<String> actual = firstUserService.saveFirstTransactionWithSupports();
        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(0)
                .containsExactly("");
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     *
     * Support 와 비슷하지만, 상위 스코프에 트랜잭션이 없는 경우 메서드 본문이 실행되지 않고 예외가 발생한다. 이렇게 예외가 발생한 경우에도 트랜잭션이 적용되지 않으므로
     * 상위 스코프에서 변경한 내용이 롤백되지 않는다.
     */
    @Test
    void testMandatory() {
        Set<String> actual = firstUserService.saveFirstTransactionWithMandatory();
        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(0)
                .containsExactly("");
    }

    /**
     * 아래 테스트는 몇 개의 물리적 트랜잭션이 동작할까? 2개
     * FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석 처리하자.
     * 다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이 동작할까? 1개
     *
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     *
     * 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 개념을 직접 서술한 문서를 찾지 못했다...
     * 다만, 여러 학습 테스트를 통해 다음과 같은 사실을 추론할 수 있었다.
     * 1. 물리 트랜잭션에는 적어도 1개 이상의 논리 트랜잭션이 매핑된다.
     * 2. 트랜잭션 어노테이션이 있으면 무조건 물리 트랜잭션은 생성된다.
     * 3. 스프링에는 트랜잭션의 활성화 개념이 있다. 트랜잭션이 활성화되어있지 않으면, 예외가 발생해도 롤백되지 않는다.
     * 4. 스프링 공식 문서에서 트랜잭션 전파 옵션 서술에서 트랜잭션을 사용하지 않는다는 말은, 논리 트랜잭션이 활성화되지 않는다는 말이다. 항상 논리 트랜잭션과 이것이 매핑될 물리 트랜잭션이 생기긴 한다.
     */
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(0)
                .containsExactly("");
    }

    /**
     * 아래 테스트는 왜 실패할까?
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     *
     * nested 설정은 상위 스코프에 트랜잭션이 있는 경우, 내부 트랜잭션을 시작하기 전에 그 시점을 save point 로 지정해, 롤백이 필요할 경우 그 시점으로
     * 돌아가게 하는 설정이다. 상위 스코프에 트랜잭션이 없는 경우 REQUIRED 옵션과 동일하게 동작한다.
     * 이 기능은 JDBC savepoints 에 의해 동작한다. 따라서 JDBC를 이용한 경우에만 동작한다.
     *
     * 하이버네이트는 변경 감지 기능의 효율성을 위해 JDBC savepoints를 지원하지 않아 사용할 수 없다.
     */
    @Test
    void testNested() {
        final var actual = firstUserService.saveFirstTransactionWithNested();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(0)
                .containsExactly("");
    }

    /**
     * 마찬가지로 @Transactional을 주석처리하면서 관찰해보자.
     *
     * 트랜잭션을 사용하지 않는다는 옵션이다. 상위 스코프에 활성화된 트랜잭션이 적용중이라면 예외가 발생한다.
     * 즉, 상위 스코프 트랜잭션이 Propagation.SUPPORTS 로 지정되어있고, 상위의 상위 트랜잭션이 없는 경우 예외가 발생하지 않는다.
     *
     * 물리 트랜잭션은 1개가 존재한다. 다만, 그 물리 트랜잭션에 매핑된 논리 트랜잭션이 활성화되지 않는 것이다.
     */
    @Test
    void testNever() {
        final var actual = firstUserService.saveFirstTransactionWithNever();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(0)
                .containsExactly("");
    }
}
