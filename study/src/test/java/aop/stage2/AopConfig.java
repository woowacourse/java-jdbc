package aop.stage2;

import aop.repository.UserDao;
import aop.repository.UserHistoryDao;
import aop.stage1.TransactionAdvice;
import aop.stage1.TransactionAdvisor;
import aop.stage1.TransactionPointcut;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableAspectJAutoProxy
public class AopConfig {

    @Bean
    public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator proxyCreator = new DefaultAdvisorAutoProxyCreator();
        proxyCreator.setProxyTargetClass(true);

        return proxyCreator;
    }

    @Bean
    public TransactionAdvisor transactionAdvisor(
            TransactionPointcut transactionPointcut,
            TransactionAdvice transactionAdvice
    ) {
        return new TransactionAdvisor(transactionPointcut, transactionAdvice);
    }

    @Bean
    public TransactionPointcut transactionPointcut() {
        return new TransactionPointcut();
    }

    @Bean
    public TransactionAdvice transactionAdvice(
            PlatformTransactionManager platformTransactionManager
    ) {
        return new TransactionAdvice(platformTransactionManager);
    }
}
