package aop.stage1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import aop.DataAccessException;
import aop.StubUserHistoryDao;
import aop.domain.User;
import aop.repository.UserDao;
import aop.repository.UserHistoryDao;
import org.aopalliance.aop.Advice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.Pointcut;
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

    private Pointcut pointcut;
    private Advice advice;

    @BeforeEach
    void setUp() {
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);

        // advice: 부가 기능을 담는 클래스
        // --> 트랜잭션 로직을 이 곳에 담는다.
        advice = new TransactionAdvice(platformTransactionManager);

        // pointcut: 부가 기능(advice)을 적용시킬지 말지 판단한다.
        // --> @Transactional이 붙어있는 경우 부가 기능을 적용시킨다.
        pointcut = new TransactionPointcut();
    }

    @Test
    void testChangePassword() {
        // advisor: advice랑 pointcut을 담고 있음.
        TransactionAdvisor advisor = new TransactionAdvisor(pointcut, advice);

        // ProxyFactoryBean 생성 및 설정
        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setTarget(new UserService(userDao, userHistoryDao));
        proxyFactoryBean.addAdvisor(advisor);

        final UserService proxyUserService = (UserService) proxyFactoryBean.getObject();

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        proxyUserService.changePassword(1L, newPassword, createBy);

        final var actual = proxyUserService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        // advisor: advice랑 pointcut을 담고 있음.
        TransactionAdvisor advisor = new TransactionAdvisor(pointcut, advice);

        // ProxyFactoryBean 생성 및 설정
        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setTarget(new UserService(userDao, stubUserHistoryDao));
        proxyFactoryBean.addAdvisor(advisor);

        final UserService proxyUserService = (UserService) proxyFactoryBean.getObject();

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        assertThrows(DataAccessException.class,
                () -> proxyUserService.changePassword(1L, newPassword, createBy));

        final var actual = proxyUserService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
