package aop.stage1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import aop.DataAccessException;
import aop.StubUserHistoryDao;
import aop.domain.User;
import aop.repository.UserDao;
import aop.repository.UserHistoryDao;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactoryBean;
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

    @Test
    void testChangePassword() {
        UserService userService = getUserService(new UserService(userDao, userHistoryDao));

        User gugu = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(gugu);
        User userGugu = userDao.findByAccount(gugu.getAccount());

        String newPassword = "newPassword";
        String createBy = "gugu";
        userService.changePassword(userGugu.getId(), newPassword, createBy);

        User actual = userService.findById(userGugu.getId());

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        UserService userService = getUserService(new UserService(userDao, stubUserHistoryDao));

        User jojo = new User("jojo", "password", "jojo@woowahan.com");
        userDao.insert(jojo);
        User userJojo = userDao.findByAccount(jojo.getAccount());

        String newPassword = "newPassword";
        String createBy = "jojo";
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(userJojo.getId(), newPassword, createBy));

        User actual = userService.findById(userJojo.getId());

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }

    private UserService getUserService(UserService target) {
        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setTarget(target);
        proxyFactoryBean.setProxyTargetClass(true);

        TransactionPointcut pointcut = new TransactionPointcut();
        TransactionAdvice advice = new TransactionAdvice(platformTransactionManager);
        proxyFactoryBean.addAdvisor(new TransactionAdvisor(pointcut, advice));

        return (UserService) proxyFactoryBean.getObject();
    }
}
