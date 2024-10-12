package transaction.stage2;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

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
    private FirstUserService firstUserService;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAll();
    }

    /**
     * 생성된 트랜잭션이 몇 개인가? 1개
     * 왜 그런 결과가 나왔을까?
     * 물리 트랜잭션 개수: 1개
     * 논리 트랜잭션 개수: 2개
     * 상태: Propagation.REQUIRED 트랜잭션 안에 Propagation.REQUIRED 가 있다.
     * REQUIRED는 이미 트랜잭션이 존재하면 외부 (물리) 트랜잭션에 참여한다.
     * 논리 트랜잭션은 Propagation.REQUIRED 세팅이 붙은 모든 메서드에서 생성된다.
     */
    @Test
    void testRequired() {
        final List<String> actual = firstUserService.saveFirstTransactionWithRequired()
                .stream()
                .toList();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly(actual.get(0));
    }

    /**
     * 생성된 트랜잭션이 몇 개인가? 2개
     * 왜 그런 결과가 나왔을까?
     * 물리 트랜잭션 개수: 2개
     * 논리 트랜잭션 개수: ?개
     * 상태: Propagation.REQUIRED 트랜잭션 안에 Propagation.REQUIRES_NEW 가 있다.
     * REQUIRES_NEW 는 항상 새로운 물리적 트랜잭션을 사용한다.
     */
    @Test
    void testRequiredNew() {
        final List<String> actual = firstUserService.saveFirstTransactionWithRequiredNew()
                .stream()
                .toList();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly(actual.get(0), actual.get(1));
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는 지 확인해보자.
     * 상태: REQUIRES 안에 REQUIRES_NEW 가 있다.
     * REQUIRES_NEW 때문에 두개의 트랜잭션이 생기는데,
     * 첫번째 트랜잭션 안에 있는 두번째 트랜잭션은 성공적으로 커밋된다.
     * 두번째 트랜잭션이 커밋된 뒤 첫번째 트랜잭션에서 에러가 발생하기 때문에 첫번째 트랜잭션은 롤백된다.
     * 그래서 결국 하나의 데이터만 저장된다.
     * 하위 스코프에서 롤백이 발생하면 외부 트랜잭션에 영향을 미친다.
     * 새로운 물리적 트랜잭션이 생성되어도 같은 스레드 안에서 실행되기 때문이다.
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
     * 주석일 때:
     * 트랜잭션이 없기 때문에 REQUIRED 속성에 따라 트랜잭션이 하나 생성되고,
     * 그 안에 있는 메서드는 SUPPORTS 속성이므로 이미 생성된 트랜잭션을 사용한다.
     * 그래서 size는 1이고, 첫번째 트랜잭션만 포함되어 있는 것을 알 수 있다.
     * 주석이 아닐 때:
     * 부모 트랜잭션을 사용한다. 따라서 총 1개의 트랜잭션이 생성된다.
     */
    @Test
    void testSupports() {
        final List<String> actual = firstUserService.saveFirstTransactionWithSupports()
                .stream()
                .toList();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly(actual.get(0));
    }

    /**
     * FirstUserService.saveFirstTransactionWithMandatory() 메서드를 보면 @Transactional이 주석으로 되어 있다.
     * 주석인 상태에서 테스트를 실행했을 때와 주석을 해제하고 테스트를 실행했을 때 어떤 차이점이 있는지 확인해보자.
     * SUPPORTS와 어떤 점이 다른지도 같이 챙겨보자.
     * 주석일 때:
     * 트랜잭션이 없기 때문에 MANDATORY 속성에 따라 예외가 발생한다.
     * - No existing transaction found for transaction marked with propagation 'mandatory'
     * 주석이 아닐 때:
     * REQUIRED 에 따라 트랜잭션이 생성되고, MANDATORY 속성에 따라 이미 생성된 트랜잭션을 사용한다.
     * 총 1개의 트랜잭션이 생성된다.
     */
    @Test
    void testMandatory() {
        final List<String> actual = firstUserService.saveFirstTransactionWithMandatory()
                .stream()
                .toList();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly(actual.get(0));
    }

    /**
     * 아래 테스트는 몇 개의 물리적 트랜잭션이 동작할까?
     * FirstUserService.saveFirstTransactionWithNotSupported() 메서드의 @Transactional을 주석 처리하자.
     * 다시 테스트를 실행하면 몇 개의 물리적 트랜잭션이 동작할까?
     *
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     *
     * 물리적 트랜잭션: 실제 db에 적용되는 트랜잭션. db에 대한 변경사항이 커밋 or 롤백되는 단위
     * 논리적 트랜잭션: 프로그래밍 단위 트랜잭션. 하나의 물리적 트랜잭션에 여러개의 논리적 트랜잭션이 포함될 수 있음
     * 하나의 논리 트랜잭션이 롤백되면 외부의 물리 트랜잭션이 롤백되며 다른 논리 트랜잭션도 롤백된다.
     *
     * NOT_SUPPORTED
     * : 부모 트랜잭션이 존재할 경우, 부모 트랜잭션에서 동작하는 태스크는 트랜잭션이 적용되지만,
     *   NOT_SUPPORTED 설정이 된 트랜잭션 안에서는 동작하지 않는다.
     * : 부모 트랜잭션이 유무에 상관없이 명시적으로 트랜잭션이 생성되지만 트랜잭션은 동작하지 않는다.
     * 주석일 때: 트랜잭션 총 1개
     * 주석 아닐 때: 트랜잭션 총 2개
     */
    @Test
    void testNotSupported() {
        final List<String> actual = firstUserService.saveFirstTransactionWithNotSupported()
                .stream()
                .toList();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly(actual.get(0), actual.get(1));
    }

    /**
     * 아래 테스트는 왜 실패할까?
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     * NESTED : 부모 트랜잭션이 있으면 중첩 트랜잭션을 만들고, 없으면 새로운 자식 트랜잭션을 만든다.
     * 부모 트랜잭션은 자식 트랜잭션에게 영향을 미치지만, 자식 트랜잭션은 부모 트랜잭션에게 영향을 미치지 않는다.
     * 그래서 자식 트랜잭션이 실패하면 부모 트랜잭션은 롤백되지 않는다.
     * jpa는 NESTED를 지원하지 않기 때문에 JpaDialect does not support savepoints - check your JPA provider's capabilities 예외가 발생한다.
     */
    @Test
    void testNested() {
        final List<String> actual = firstUserService.saveFirstTransactionWithNested()
                .stream()
                .toList();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly(actual.get(0));
    }

    /**
     * 마찬가지로 @Transactional을 주석처리하면서 관찰해보자.
     * NEVER: 트랜잭션이 명시적으로 표현은 되지만 트랜잭션을 사용하지 않는다. 부모 트랜잭션이 존재할 경우 예외가 발생한다.
     * IllegalTransactionStateException: Existing transaction found for transaction marked with propagation 'never'
     */
    @Test
    void testNever() {
        final List<String> actual = firstUserService.saveFirstTransactionWithNever()
                .stream()
                .toList();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly(actual.get(0));
    }
}
