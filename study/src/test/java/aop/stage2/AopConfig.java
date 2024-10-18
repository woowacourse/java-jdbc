package aop.stage2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.PlatformTransactionManager;

import aop.stage1.TransactionAdvice;
import aop.stage1.TransactionAdvisor;
import aop.stage1.TransactionPointcut;

@Configuration
@EnableAspectJAutoProxy
public class AopConfig {

    @Bean
    public TransactionAdvisor transactionAdvisor(final PlatformTransactionManager transactionManager) {
        return new TransactionAdvisor(transactionManager);
    }

    @Bean
    public TransactionPointcut transactionPointcut() {
        return new TransactionPointcut();
    }

    public TransactionAdvice transactionAdvice(final PlatformTransactionManager transactionManager) {
        return new TransactionAdvice(transactionManager);
    }
}
