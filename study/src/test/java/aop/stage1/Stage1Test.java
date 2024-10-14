package aop.stage1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;
import aop.DataAccessException;
import aop.StubUserHistoryDao;
import aop.domain.User;
import aop.repository.UserDao;
import aop.repository.UserHistoryDao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    /**
     * ProxyFactoryBean: 스프링이 제공하는 클래스로,
     * - 생성자가 아닌 다른 방식으로 스프링 빈을ㅇ 생성할 때 사용한다.
     * - 프록시 객체를 생성한다.
     * - default: JDK Proxy, setProxyTargetClass = true이면 CGLib
     */
    private ProxyFactoryBean proxyFactoryBean;

    @BeforeEach
    void setUp() {
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        proxyFactoryBean = new ProxyFactoryBean();
        /**
         * setProxyTargetClass(true): CGLIB 기반의 클래스 프록시를 사용하겠다는 설정
         * 인터페이스가 없는 클래스에 대해 프록시를 생성하거나, 클래슷 자체를 프록시로 만들어야 할 때 사용한다.
         */
        proxyFactoryBean.setProxyTargetClass(true);
        // TransactionAdvisor를 ProxyFactoryBean에 추가하여 트랜잭션 어드바이스를 타깃 객체에 적용
        proxyFactoryBean.addAdvisor(new TransactionAdvisor(new TransactionPointcut(), new TransactionAdvice(platformTransactionManager)));
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        proxyFactoryBean.setTarget(new UserService(userDao, userHistoryDao));
        UserService userService = (UserService) proxyFactoryBean.getObject();

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        proxyFactoryBean.setTarget(new UserService(userDao, stubUserHistoryDao));
        UserService userService = (UserService) proxyFactoryBean.getObject();

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
