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

/*
# ProxyFactoryBean 도입하기
### 장점
- Proxy 로 생성한 객체를 스프링 빈에 등록 가능함
- 매번 인터페이스 만들지 않아도 됨
- 메서드 외에 클래스도 지정 가능함
- 프록시 생성 방법(JDK Proxy 또는 CGLib) 정할 수 있음
*/
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

    private TransactionPointcut pointcut;

    private TransactionAdvice advice;

    @BeforeEach
    void setUp() {
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
        pointcut = new TransactionPointcut();
        advice = new TransactionAdvice(platformTransactionManager);
    }

    @Test
    void testChangePassword() {
        // given
        final ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setTarget(new UserService(userDao, userHistoryDao));
        proxyFactoryBean.setProxyTargetClass(true); // true면 대상 클래스가 인터페이스일 때는 jdk proxy를, 클래스일 때는 cglib 사용
        proxyFactoryBean.addAdvisor(new TransactionAdvisor(pointcut, advice));
        final UserService userService = (UserService) proxyFactoryBean.getObject();

        // when
        final String newPassword = "qqqqq";
        final String createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        // then
        final User actual = userService.findById(1L);
        assertThat(actual.getPassword()).isEqualTo(newPassword);
    }

    @Test
    void testTransactionRollback() {
        // given
        final ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setTarget(new UserService(userDao, stubUserHistoryDao));
        proxyFactoryBean.setProxyTargetClass(true); // true면 대상 클래스가 인터페이스일 때는 jdk proxy를, 클래스일 때는 cglib 사용
        proxyFactoryBean.addAdvisor(new TransactionAdvisor(pointcut, advice));
        final UserService userService = (UserService) proxyFactoryBean.getObject();

        // when
        final var newPassword = "newPassword";
        final var createBy = "gugu";
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));
        final User actual = userService.findById(1L);

        // then
        assertThat(actual.getPassword()).isNotEqualTo(newPassword);
    }
}
