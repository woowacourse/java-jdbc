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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class Stage1Test {

    private static final Logger log = LoggerFactory.getLogger(Stage1Test.class);

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
        UserService userService = new UserService(userDao, userHistoryDao);

        String newPassword = "qqqqq";
        String createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        User actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        UserService userService = new UserService(userDao, stubUserHistoryDao);
        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setTarget(userService);
        proxyFactoryBean.setProxyTargetClass(false);

        TransactionPointcut transactionPointcut = new TransactionPointcut();
        TransactionAdvice transactionAdvice = new TransactionAdvice(platformTransactionManager);
        TransactionAdvisor transactionAdvisor = new TransactionAdvisor(transactionPointcut, transactionAdvice);
        proxyFactoryBean.addAdvisor(transactionAdvisor);

        UserService proxyUserService = (UserService) proxyFactoryBean.getObject();

        String newPassword = "newPassword";
        String createBy = "gugu";
        assertThrows(DataAccessException.class,
                () -> proxyUserService.changePassword(1L, newPassword, createBy));

        User actual = proxyUserService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
