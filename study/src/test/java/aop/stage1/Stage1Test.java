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

### 과정
1. ProxyFactoryBean 객체를 생성
    - 프록시할 타겟 빈을 설정
    - 프록시 생성 방식 설정
    - 프록시 객체에 부가기능을 적용할 Advisor를 추가
2. getObject()를 호출하여 프록시 객체를 가져옴
    - UserService처럼 동작하지만, 메서드 호출 시 트랜잭션 관리가 추가된 프록시 객체
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

    private TransactionPointcut pointcut; // 어드바이스(부가기능)를 적용할 조인 포인트를 선별하는 클래스

    private TransactionAdvice advice; //  부가기능을 담고 있는 클래스

    @BeforeEach
    void setUp() {
        final var user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
        pointcut = new TransactionPointcut(); // 메서드에 트랜잭션 어노테이션 붙어있다면 부가 기능 적용
        advice = new TransactionAdvice(platformTransactionManager); // 부가 기능으로 트랜잭션 적용
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
