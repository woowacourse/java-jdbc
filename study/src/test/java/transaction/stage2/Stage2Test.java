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
     *
     * transaction.stage2.FirstUserService.saveFirstTransactionWithRequired is Actual Transaction Active : ✅ true
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default) ⬅️ userRepository.save(User.createTest()) 실행
     * 2024-10-09T18:46:18.397+09:00  INFO 36327 --- [    Test worker] transaction.stage2.SecondUserService     :
     * transaction.stage2.FirstUserService.saveFirstTransactionWithRequired is Actual Transaction Active : ✅ true
     *
     * propagation = Propagation.REQUIRED 로 옵션 설정 시 기존 트랜잭션이 있으면 해당 트랜잭션을 사용하고 없으면 새로운 트랜잭션을 생성한다.
     * 고로 secondUserService의 saveSecondTransactionWithRequired는 saveFirstTransactionWithRequired 이어서 사용
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
     *
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-09T18:49:15.296+09:00  INFO 36396 --- [    Test worker] transaction.stage2.FirstUserService      :
     * transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew is Actual Transaction Active : ✅ true
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-09T18:49:15.298+09:00  INFO 36396 --- [    Test worker] transaction.stage2.SecondUserService     :
     * transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew is Actual Transaction Active : ✅ true
     * 2024-10-09T18:49:15.302+09:00  INFO 36396 --- [    Test worker] transaction.stage2.Stage2Test            : transactions : [transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew, transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew]
     * Hibernate: select u1_0.id,u1_0.account,u1_0.email,u1_0.password from users u1_0
     * Hibernate: delete from users where id=?
     * Hibernate: delete from users where id=?
     *
     *  propagation = Propagation.REQUIRES_NEW 로 옵션 설정 시 무조건 새로운 트랜잭션을 생성한다.
     *  고로 secondUserService의 saveSecondTransactionWithRequiresNew는 saveFirstTransactionWithRequiredNew 이어서 사용하지 않고 새로 생성
     */
    @Test
    void testRequiredNew() {
        final var actual = firstUserService.saveFirstTransactionWithRequiredNew();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(2)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew", "transaction.stage2.FirstUserService.saveFirstTransactionWithRequiredNew");
    }

    /**
     * firstUserService.saveAndExceptionWithRequiredNew()에서 강제로 예외를 발생시킨다.
     * REQUIRES_NEW 일 때 예외로 인한 롤백이 발생하면서 어떤 상황이 발생하는 지 확인해보자.
     *
     * Hibernate: select u1_0.id,u1_0.account,u1_0.email,u1_0.password from users u1_0
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-09T18:55:00.314+09:00  INFO 36508 --- [    Test worker] transaction.stage2.SecondUserService     :
     * transaction.stage2.SecondUserService.saveSecondTransactionWithRequiresNew is Actual Transaction Active : ✅ true
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-09T18:55:00.318+09:00  INFO 36508 --- [    Test worker] transaction.stage2.FirstUserService      :
     * transaction.stage2.FirstUserService.saveAndExceptionWithRequiredNew is Actual Transaction Active : ✅ true
     * Hibernate: select u1_0.id,u1_0.account,u1_0.email,u1_0.password from users u1_0
     * Hibernate: select u1_0.id,u1_0.account,u1_0.email,u1_0.password from users u1_0
     * Hibernate: delete from users where id=?
     *
     * REQUIRES_NEW로 설정된 트랜잭션은 예외가 발생하면 롤백되고, 외부 트랜잭션에 영향을 주지 않는다.
     * secondUserService.saveSecondTransactionWithRequiresNew 는 자기만 롤백 되고 firstUserService.saveAndExceptionWithRequiredNew 는 롤백되지 않는다.
     *
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
     * [주석 있는 경우]
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-09T18:56:59.905+09:00  INFO 36555 --- [    Test worker] transaction.stage2.FirstUserService      :
     * null is Actual Transaction Active : ❌ false
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-09T18:56:59.907+09:00  INFO 36555 --- [    Test worker] transaction.stage2.SecondUserService     :
     * transaction.stage2.SecondUserService.saveSecondTransactionWithSupports is Actual Transaction Active : ❌ false
     * 2024-10-09T18:56:59.907+09:00  INFO 36555 --- [    Test worker] transaction.stage2.Stage2Test            : transactions : [transaction.stage2.SecondUserService.saveSecondTransactionWithSupports]
     * Hibernate: select u1_0.id,u1_0.account,u1_0.email,u1_0.password from users u1_0
     * Hibernate: delete from users where id=?
     * Hibernate: delete from users where id=?
     *
     * 기본 전파레벨은 REQUIRED 이다.
     * SUPPORTS로 설정하면 이미 진행 중인 트랜잭션이 있으면 해당 트랜잭션을 사용하고 없으면 트랜잭션을 생성하지 않는다.
     * 고로 firstUserService.saveFirstTransactionWithSupports가 트랜잭션 없이 실행되고
     * secondUserService.saveSecondTransactionWithSupports는 이어서 트랜잭션 없이 실행된다.
     *
     *
     * [주석 없는 경우]
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-09T18:57:37.321+09:00  INFO 36565 --- [    Test worker] transaction.stage2.FirstUserService      :
     * transaction.stage2.FirstUserService.saveFirstTransactionWithSupports is Actual Transaction Active : ✅ true
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-09T18:57:37.322+09:00  INFO 36565 --- [    Test worker] transaction.stage2.SecondUserService     :
     * transaction.stage2.FirstUserService.saveFirstTransactionWithSupports is Actual Transaction Active : ✅ true
     * 2024-10-09T18:57:37.326+09:00  INFO 36565 --- [    Test worker] transaction.stage2.Stage2Test            : transactions : [transaction.stage2.FirstUserService.saveFirstTransactionWithSupports]
     * Hibernate: select u1_0.id,u1_0.account,u1_0.email,u1_0.password from users u1_0
     * Hibernate: delete from users where id=?
     * Hibernate: delete from users where id=?
     *
     * SUPPORTS로 설정하면 이미 진행 중인 트랜잭션이 있으면 해당 트랜잭션을 사용하고 없으면 트랜잭션을 생성하지 않는다.
     * 고로 firstUserService.saveFirstTransactionWithSupports가 트랜잭션이 있이 실행되기 때문에 SecondUserService도 같은 트랜잭션을 사용한다.
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
     * [주석 있는 경우]
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-09T19:13:40.183+09:00  INFO 36708 --- [    Test worker] transaction.stage2.FirstUserService      :
     * null is Actual Transaction Active : ❌ false
     * Hibernate: select u1_0.id,u1_0.account,u1_0.email,u1_0.password from users u1_0
     * Hibernate: delete from users where id=?
     *
     * MANDATORY로 설정하면 이미 진행 중인 트랜잭션이 있으면 해당 트랜잭션을 사용하고 없으면 예외를 발생시킨다.
     * 고로 firstUserService.saveFirstTransactionWithMandatory가 트랜잭션이 없이 실행되기 때문에 SecondUserService는 실행되지 않고 예외를 발생한다.
     *
     * [주석 없는 경우]
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-09T19:15:19.432+09:00  INFO 36738 --- [    Test worker] transaction.stage2.FirstUserService      :
     * transaction.stage2.FirstUserService.saveFirstTransactionWithMandatory is Actual Transaction Active : ✅ true
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-09T19:15:19.433+09:00  INFO 36738 --- [    Test worker] transaction.stage2.SecondUserService     :
     * transaction.stage2.FirstUserService.saveFirstTransactionWithMandatory is Actual Transaction Active : ✅ true
     * 2024-10-09T19:15:19.437+09:00  INFO 36738 --- [    Test worker] transaction.stage2.Stage2Test            : transactions : [transaction.stage2.FirstUserService.saveFirstTransactionWithMandatory]
     * Hibernate: select u1_0.id,u1_0.account,u1_0.email,u1_0.password from users u1_0
     * Hibernate: delete from users where id=?
     * Hibernate: delete from users where id=?
     *
     * firstUserService.saveFirstTransactionWithMandatory가 트랜잭션이 있이 실행되기 때문에 SecondUserService도 같은 트랜잭션을 사용한다.
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
     * [주석 있는 경우]
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-09T19:17:23.628+09:00  INFO 36781 --- [    Test worker] transaction.stage2.FirstUserService      :
     * null is Actual Transaction Active : ❌ false
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-09T19:17:23.630+09:00  INFO 36781 --- [    Test worker] transaction.stage2.SecondUserService     :
     * transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported is Actual Transaction Active : ❌ false
     * 2024-10-09T19:17:23.630+09:00  INFO 36781 --- [    Test worker] transaction.stage2.Stage2Test            : transactions : [transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported]
     * Hibernate: select u1_0.id,u1_0.account,u1_0.email,u1_0.password from users u1_0
     * Hibernate: delete from users where id=?
     * Hibernate: delete from users where id=?
     *
     * NOT_SUPPORTED로 설정하면 이미 진행 중인 트랜잭션이 있으면 해당 트랜잭션을 일시 중지하고 트랜잭션을 생성하지 않는다.
     *
     * [주석 없는 경우]
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-09T19:24:21.705+09:00  INFO 36880 --- [    Test worker] transaction.stage2.FirstUserService      :
     * transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported is Actual Transaction Active : ✅ true
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-09T19:24:21.712+09:00  INFO 36880 --- [    Test worker] transaction.stage2.SecondUserService     :
     * transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported is Actual Transaction Active : ❌ false
     * 2024-10-09T19:24:21.712+09:00  INFO 36880 --- [    Test worker] transaction.stage2.Stage2Test            : transactions : [transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported, transaction.stage2.FirstUserService.saveFirstTransactionWithNotSupported]
     * Hibernate: select u1_0.id,u1_0.account,u1_0.email,u1_0.password from users u1_0
     * Hibernate: delete from users where id=?
     * Hibernate: delete from users where id=?
     *
     * FirstUserService의 saveFirstTransactionWithNotSupported는 트랜잭션이 동작하고 SecondUserService의 saveSecondTransactionWithNotSupported는 기존 트랜잭션이 즉시 종료시킨다.
     *
     * -------------------------------------------------------------------------------
     * 스프링 공식 문서에서 물리적 트랜잭션과 논리적 트랜잭션의 차이점이 무엇인지 찾아보자.
     *
     * [물리적 트랜잭션 (Physical Transaction)]
     * 실제 데이터베이스 연결에 대한 트랜잭션을 의미
     * 데이터베이스와의 실제 통신 및 커밋/롤백 작업을 수행합니다.
     * 일반적으로 하나의 물리적 트랜잭션이 존재합니다.
     *
     *
     * [논리적 트랜잭션 (Logical Transaction)]
     * 스프링의 트랜잭션 추상화 레벨에서 사용되는 개념
     * @Transactional 어노테이션이 적용된 각 메서드는 하나의 논리적 트랜잭션을 나타냅니다.
     * 여러 개의 논리적 트랜잭션이 중첩될 수 있습니다.
     *
     * transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported가 물리적 트랜잭션으로 실행된다.
     */
    @Test
    void testNotSupported() {
        final var actual = firstUserService.saveFirstTransactionWithNotSupported();

        log.info("transactions : {}", actual);
        assertThat(actual)
                .hasSize(1)
                .containsExactly("transaction.stage2.SecondUserService.saveSecondTransactionWithNotSupported가");
    }

    /**
     * 아래 테스트는 왜 실패할까?
     * FirstUserService.saveFirstTransactionWithNested() 메서드의 @Transactional을 주석 처리하면 어떻게 될까?
     *
     * [주석 없음]
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-09T19:32:29.237+09:00  INFO 37048 --- [    Test worker] transaction.stage2.FirstUserService      :
     * transaction.stage2.FirstUserService.saveFirstTransactionWithNested is Actual Transaction Active : ✅ true
     * Hibernate: select u1_0.id,u1_0.account,u1_0.email,u1_0.password from users u1_0
     *
     * JpaDialect does not support savepoints - check your JPA provider's capabilities
     * org.springframework.transaction.NestedTransactionNotSupportedException: JpaDialect does not support savepoints - check your JPA provider's capabilities
     * Propagation.NESTED를 사용하려면 트랜잭션 중간에 savepoint를 설정할 수 있어야 합니다. 그러나 일반적으로 Hibernate와 같은 JPA 구현체는 savepoint 기능을 기본적으로 지원하지 않기 때문에
     * Spring이 NestedTransactionNotSupportedException을 발생시킨다.
     * 그런데 외부 트랜잭션이 있어서 내부의 NESTED 옵션의 트랜잭셔널이 적용되려면서 오류가 나는가보다
     *
     * [주석 있음]
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-09T19:33:57.418+09:00  INFO 37070 --- [    Test worker] transaction.stage2.FirstUserService      :
     * null is Actual Transaction Active : ❌ false
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-09T19:33:57.419+09:00  INFO 37070 --- [    Test worker] transaction.stage2.SecondUserService     :
     * transaction.stage2.SecondUserService.saveSecondTransactionWithNested is Actual Transaction Active : ✅ true
     * 2024-10-09T19:33:57.420+09:00  INFO 37070 --- [    Test worker] transaction.stage2.Stage2Test            : transactions : [transaction.stage2.SecondUserService.saveSecondTransactionWithNested]
     * Hibernate: select u1_0.id,u1_0.account,u1_0.email,u1_0.password from users u1_0
     * Hibernate: delete from users where id=?
     * Hibernate: delete from users where id=?
     *
     * 프록시 때문에 이너 서비스의 Transaction이 먹히지 않아 오류가 발생하지 않는다
     */
    @Test
    void testNested() {
        final var actual = firstUserService.saveFirstTransactionWithNested();

        log.info("transactions : {}", actual);
        assertThat(actual).isEmpty();
    }

    /**
     * 마찬가지로 @Transactional을 주석처리하면서 관찰해보자.
     *
     * [주석 없음]
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-09T19:36:55.221+09:00  INFO 37142 --- [    Test worker] transaction.stage2.FirstUserService      :
     * transaction.stage2.FirstUserService.saveFirstTransactionWithNever is Actual Transaction Active : ✅ true
     * Hibernate: select u1_0.id,u1_0.account,u1_0.email,u1_0.password from users u1_0
     *
     * NEVER로 설정하면 이미 진행 중인 트랜잭션이 있으면 예외를 발생시킨다.
     * Existing transaction found for transaction marked with propagation 'never'
     * org.springframework.transaction.IllegalTransactionStateException: Existing transaction found for transaction marked with propagation 'never'
     * IllegalTransactionStateException가 발생한다.
     *
     * [주석 있음]
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-09T19:37:28.982+09:00  INFO 37153 --- [    Test worker] transaction.stage2.FirstUserService      :
     * null is Actual Transaction Active : ❌ false
     * Hibernate: insert into users (account,email,password,id) values (?,?,?,default)
     * 2024-10-09T19:37:28.984+09:00  INFO 37153 --- [    Test worker] transaction.stage2.SecondUserService     :
     * transaction.stage2.SecondUserService.saveSecondTransactionWithNever is Actual Transaction Active : ❌ false
     * 2024-10-09T19:37:28.984+09:00  INFO 37153 --- [    Test worker] transaction.stage2.Stage2Test            : transactions : [transaction.stage2.SecondUserService.saveSecondTransactionWithNever]
     * Hibernate: select u1_0.id,u1_0.account,u1_0.email,u1_0.password from users u1_0
     * Hibernate: delete from users where id=?
     * Hibernate: delete from users where id=?
     *
     * 진행 중인 트랜잭션이 없기 때문에 에러는 발생하지 않고 SecondUserService의 트랜잭션이 실행된다.
     * transaction.stage2.SecondUserService.saveSecondTransactionWithNever가 물리적 트랜잭션으로 실행
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
