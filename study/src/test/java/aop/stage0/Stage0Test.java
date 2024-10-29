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

    @Test
    void testChangePassword() {
        final var appUserService = new AppUserService(userDao, userHistoryDao);

        // JDK Proxy를 활용하여 UserService 인터페이스를 기반으로 프록시 생성
        Object proxyUserService = Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{UserService.class},
                // TransactionHandler를 이용해 트랜잭션 적용
                new TransactionHandler(appUserService, platformTransactionManager)
        );
        final UserService userService = (UserService) proxyUserService;
        final var newPassword = "qqqqq";
        final var createBy = "gugu";

        userService.changePassword(1L, newPassword, createBy);

        final var actual = userService.findById(1L);
        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        final var appUserService = new AppUserService(userDao, stubUserHistoryDao);
        Object proxyUserService = Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{UserService.class},
                new TransactionHandler(appUserService, platformTransactionManager)
        );
        final UserService userService = (UserService) proxyUserService;
        final var newPassword = "newPassword";
        final var createBy = "gugu";

        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final var actual = userService.findById(1L);
        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
