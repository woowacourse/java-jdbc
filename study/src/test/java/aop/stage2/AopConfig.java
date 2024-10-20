package aop.stage2;

import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import aop.stage1.TransactionAdvice;
import aop.stage1.TransactionAdvisor;
import aop.stage1.TransactionPointcut;

@Configuration
public class AopConfig {

    @Bean
    public TransactionPointcut transactionPointcut() {
        return new TransactionPointcut();
    }

    @Bean
    public TransactionAdvice transactionAdvice(PlatformTransactionManager transactionManager) {
        return new TransactionAdvice(transactionManager);
    }

    @Bean
    public TransactionAdvisor transactionAdvisor(TransactionPointcut pointcut, TransactionAdvice advice) {
        return new TransactionAdvisor(pointcut, advice);
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }
}
