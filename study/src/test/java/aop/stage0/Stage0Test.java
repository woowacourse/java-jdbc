package aop.stage0;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class Stage0Test {

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
        User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        UserService appUserService = new AppUserService(userDao, userHistoryDao);
        UserService userService = (UserService) Proxy.newProxyInstance(
                appUserService.getClass().getClassLoader(),
                new Class<?>[]{UserService.class},
                new TransactionHandler(appUserService, platformTransactionManager)
        );

        String newPassword = "qqqqq";
        String createdBy = "gugu";
        userService.changePassword(1L, newPassword, createdBy);
        User actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        UserService appUserService = new AppUserService(userDao, stubUserHistoryDao);
        UserService userService = (UserService) Proxy.newProxyInstance(
                appUserService.getClass().getClassLoader(),
                new Class<?>[]{UserService.class},
                new TransactionHandler(appUserService, platformTransactionManager)
        );

        String newPassword = "newPassword";
        String createdBy = "gugu";
        assertThatThrownBy(() -> userService.changePassword(1L, newPassword, createdBy))
                .isInstanceOf(DataAccessException.class);

        User actual = userService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
