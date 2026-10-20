package aop.stage2;

import aop.repository.UserDao;
import aop.repository.UserHistoryDao;
import aop.stage1.TransactionAdvice;
import aop.stage1.TransactionAdvisor;
import aop.stage1.TransactionPointcut;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AopConfig {

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserHistoryDao userHistoryDao;

    @Bean
    public UserService userService() {
        final var target = new UserService(userDao, userHistoryDao);
        final var proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setTarget(target);
        proxyFactoryBean.setProxyTargetClass(true);

        final var pointcut = new TransactionPointcut();
        final var advice = new TransactionAdvice(platformTransactionManager);
        proxyFactoryBean.addAdvisor(new TransactionAdvisor(pointcut, advice));

        return (UserService) proxyFactoryBean.getObject();
    }
}
