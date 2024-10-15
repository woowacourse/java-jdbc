package aop.stage0;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import java.lang.reflect.Proxy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;

import aop.DataAccessException;
import aop.StubUserHistoryDao;
import aop.domain.User;
import aop.repository.UserDao;
import aop.repository.UserHistoryDao;
import aop.service.AppUserService;
import aop.service.UserService;

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
        final User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        final UserService appUserService = new AppUserService(userDao, userHistoryDao);
        final UserService userService = (UserService) Proxy.newProxyInstance(
                UserService.class.getClassLoader(),
                new Class[]{UserService.class},
                new TransactionHandler(appUserService, platformTransactionManager)
        );

        final String newPassword = "qqqqq";
        final String createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        final User actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        final UserService appUserService = new AppUserService(userDao, stubUserHistoryDao);
        UserService userService = (UserService) Proxy.newProxyInstance(
                UserService.class.getClassLoader(),
                new Class[]{UserService.class},
                new TransactionHandler(appUserService, platformTransactionManager)
        );

        final String newPassword = "newPassword";
        final String createBy = "gugu";
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final User actual = userService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
