package aop.stage1;

import aop.DataAccessException;
import aop.StubUserHistoryDao;
import aop.domain.User;
import aop.repository.UserDao;
import aop.repository.UserHistoryDao;
import aop.service.AppUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.PlatformTransactionManager;

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

    @BeforeEach
    void setUp() {
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        //1. 프록시팩토리빈 생성 및, 타겟 설정
        final var proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setTarget(new UserService(userDao, userHistoryDao));

        //2. 클래스 대상으로 프록시 만들 때 필요한 설정= JDK대신 CGLib를 사용한다.
        proxyFactoryBean.setProxyTargetClass(true);

        //3. 포인트 컷과 어드바이스로 이뤄진 어드바이서 생성
        final var pointCut = new TransactionPointcut();
        final var advice = new TransactionAdvice(platformTransactionManager);
        proxyFactoryBean.addAdvisor(new TransactionAdvisor(pointCut, advice));

        //4. 유저서비스는 인터페이스가 아니라 구현클래스로 변경
        final UserService userService = (UserService) proxyFactoryBean.getObject();
        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        //1. 프록시팩토리빈 생성 및, 타겟 설정
        final var proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setTarget(new UserService(userDao, userHistoryDao));

        //2. 클래스 대상으로 프록시 만들 때 필요한 설정= JDK대신 CGLib를 사용한다.
        proxyFactoryBean.setProxyTargetClass(true);

        //3. 포인트 컷과 어드바이스로 이뤄진 어드바이서 생성
        final var pointCut = new TransactionPointcut();
        final var advice = new TransactionAdvice(platformTransactionManager);
        proxyFactoryBean.addAdvisor(new TransactionAdvisor(pointCut, advice));

        //4. 유저서비스는 인터페이스가 아니라 구현클래스로 변경
        final UserService userService = (UserService) proxyFactoryBean.getObject();

        final var newPassword = "newPassword";
        final var createBy = "gugu";
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final var actual = userService.findById(1L);

        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
