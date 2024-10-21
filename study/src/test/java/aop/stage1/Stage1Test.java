package aop.stage1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import aop.DataAccessException;
import aop.StubUserHistoryDao;
import aop.domain.User;
import aop.repository.UserDao;
import aop.repository.UserHistoryDao;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import transaction.stage1.jdbc.JdbcTemplate;

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

    @Autowired
    private DataSource dataSource;

    private ProxyFactoryBean proxyFactoryBean;

    @BeforeEach
    void setUp() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("DELETE FROM users;");
        jdbcTemplate.update("ALTER TABLE users AUTO_INCREMENT=1;");
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
        proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setProxyTargetClass(true);
        Advisor advisor = new TransactionAdvisor(
                new TransactionPointcut(),
                new TransactionAdvice(platformTransactionManager)
        );
        proxyFactoryBean.addAdvisor(advisor);
    }

    @Test
    void testChangePassword() {
        proxyFactoryBean.setTarget(new UserService(userDao, userHistoryDao));
        UserService userService = (UserService) proxyFactoryBean.getObject();

        String newPassword = "newPassword";
        String createdBy = "gugu";
        userService.changePassword(1L, newPassword, createdBy);

        User actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        proxyFactoryBean.setTarget(new UserService(userDao, stubUserHistoryDao));
        UserService userService = (UserService) proxyFactoryBean.getObject();

        String newPassword = "newPassword";
        String createdBy = "gugu";
        assertThatThrownBy(() -> userService.changePassword(1L, newPassword, createdBy))
                .isInstanceOf(DataAccessException.class);
        User actual = userService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
