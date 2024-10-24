package aop.stage2;

import aop.repository.UserDao;
import aop.repository.UserHistoryDao;
import aop.stage1.TransactionAdvisor;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement(proxyTargetClass = true)
@Configuration
public class AopConfig {

    @Primary
    @Bean
    public ProxyFactoryBean userServiceProxy(
            UserDao userDao,
            UserHistoryDao userHistoryDao,
            PlatformTransactionManager transactionManager) {

        ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();

        UserService target = new UserService(userDao, userHistoryDao);
        proxyFactoryBean.setTarget(target);

        TransactionAdvisor advisor = new TransactionAdvisor(transactionManager);
        proxyFactoryBean.addAdvisor(advisor);

        return proxyFactoryBean;
    }
}
