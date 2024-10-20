package aop.stage2;

import aop.stage1.TransactionAdvice;
import aop.stage1.TransactionAdvisor;
import aop.stage1.TransactionPointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AopConfig {

    @Bean
    public TransactionAdvisor transactionAdvisor(final PlatformTransactionManager transactionManager) {
        final TransactionAdvice transactionAdvice = new TransactionAdvice(transactionManager);
        final TransactionPointcut transactionPointcut = new TransactionPointcut();
        return new TransactionAdvisor(transactionAdvice, transactionPointcut);
    }
}
