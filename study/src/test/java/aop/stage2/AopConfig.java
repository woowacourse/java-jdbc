package aop.stage2;

import aop.stage1.TransactionAdvice;
import aop.stage1.TransactionAdvisor;
import aop.stage1.TransactionPointcut;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AopConfig {

    @Bean
    public TransactionPointcut pointcut() {
        return new TransactionPointcut();
    }

    @Bean
    public TransactionAdvice advice(PlatformTransactionManager transactionManager) {
        return new TransactionAdvice(transactionManager);
    }

    @Bean
    public TransactionAdvisor advisor(TransactionPointcut pointcut, TransactionAdvice advice) {
        return new TransactionAdvisor(pointcut, advice);
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator proxyCreator() {
        DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        defaultAdvisorAutoProxyCreator.setProxyTargetClass(true);
        return defaultAdvisorAutoProxyCreator;
    }
}
