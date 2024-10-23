package aop.stage2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import aop.stage1.TransactionAdvice;
import aop.stage1.TransactionAdvisor;
import aop.stage1.TransactionPointcut;

@Configuration
public class AopConfig {

    @Bean
    public TransactionAdvisor transactionAdvisor(PlatformTransactionManager transactionManager) {
        final var advice = new TransactionAdvice(transactionManager);
        final var pointcut = new TransactionPointcut();
        return new TransactionAdvisor(pointcut, advice);
    }

//    @Bean
//    public DefaultAdvisorAutoProxyCreator defaultTransactionAdvisor() {
//        return new DefaultAdvisorAutoProxyCreator();
//    }
}
