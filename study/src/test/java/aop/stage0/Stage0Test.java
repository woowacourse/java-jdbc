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
    private PlatformTransactionManager transactionManager;

    @BeforeEach
    void setUp() {
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        AppUserService appUserService = new AppUserService(userDao, userHistoryDao);
        UserService userService = (UserService) Proxy.newProxyInstance(
                UserService.class.getClassLoader(),
                new Class[]{UserService.class},
                new TransactionHandler(transactionManager, appUserService)
        );

        String newPassword = "qqqqq";
        String createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        User actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        AppUserService appUserService = new AppUserService(userDao, stubUserHistoryDao);
        UserService userService = (UserService) Proxy.newProxyInstance(
                UserService.class.getClassLoader(),
                new Class[]{UserService.class},
                new TransactionHandler(transactionManager, appUserService)
        );

        String newPassword = "newPassword";
        String createBy = "gugu";
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        User actual = userService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
