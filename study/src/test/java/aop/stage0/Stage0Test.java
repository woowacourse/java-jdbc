package aop.stage0;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import aop.DataAccessException;
import aop.StubUserHistoryDao;
import aop.domain.User;
import aop.repository.UserDao;
import aop.repository.UserHistoryDao;
import aop.service.AppUserService;
import aop.service.UserService;
import java.lang.reflect.Proxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;

/*
# JDK Proxy로 프록시 적용하기
### 과정
1. 클라이언트가 인터페이스의 메서드를 호출
2. 동적으로 생성된 프록시 객체가 호출을 가로챔
3. 프록시 객체는 InvocationHandler의 invoke 메서드로 전달
4. InvocationHandler는 대상 객체의 메서드를 호출
5. 결과를 클라이언트에 반환

### 단점
- Proxy로 생성한 객체의 타입은 UserService고, AppUserService가 아니라서 스프링 빈으로 등록 불가함
- 매번 인터페이스 만들고, 프록시 적용할 InvocationHandler 구현해야 함
- 메서드만 프록시 적용할 수 있음
*/
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class Stage0Test {

    private static final Logger log = LoggerFactory.getLogger(Stage0Test.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserHistoryDao userHistoryDao;

    @Autowired
    private StubUserHistoryDao stubUserHistoryDao;

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @BeforeEach
    void setUp() {
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    /*
    인터페이스로 트랜잭션, 비즈니스 로직을 분리할 땐, Service마다 TxService를 구현해야 했음
    JDK Proxy로 프록시를 사용하면, 매번 트랜잭션 클래스를 동적으로 만들어 쓸 수 있음 (TxService 구현 필요 X)
    */
    @Test
    void testChangePassword() {
        final AppUserService appUserService = new AppUserService(userDao, userHistoryDao);
        final UserService userService = (UserService) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{UserService.class},
                new TransactionHandler(platformTransactionManager, appUserService)
        );

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        final AppUserService appUserService = new AppUserService(userDao, stubUserHistoryDao);
        final UserService userService = (UserService) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{UserService.class},
                new TransactionHandler(platformTransactionManager, appUserService)
        );

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
