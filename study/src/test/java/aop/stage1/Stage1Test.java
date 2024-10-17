package aop.stage1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import aop.DataAccessException;
import aop.StubUserHistoryDao;
import aop.domain.User;
import aop.repository.UserDao;
import aop.repository.UserHistoryDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Stage1Test {

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
        final var target = new UserService(userDao, userHistoryDao);
        final var proxy = createTransactionProxy(target);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        proxy.changePassword(1L, newPassword, createBy);

        final var actual = proxy.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        final var target = new UserService(userDao, stubUserHistoryDao);
        final var proxy = createTransactionProxy(target);

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        assertThrows(DataAccessException.class,
                () -> proxy.changePassword(1L, newPassword, createBy));

        final var actual = proxy.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }

    private <T> T createTransactionProxy(T target) {
        ProxyFactory proxyFactory = new ProxyFactory(target);
        proxyFactory.addAdvisor(new TransactionAdvisor(platformTransactionManager));
        return (T) proxyFactory.getProxy();
    }
}
