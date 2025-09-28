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
    public TransactionAdvice transactionAdvice(PlatformTransactionManager transactionManager) {
        return new TransactionAdvice(transactionManager);
    }

    @Bean
    public TransactionPointcut transactionPointcut() {
        return new TransactionPointcut();
    }

    @Bean
    public TransactionAdvisor transactionAdvisor(
            TransactionPointcut transactionPointcut,
            TransactionAdvice transactionAdvice
    ) {
        return new TransactionAdvisor(transactionPointcut, transactionAdvice);
    }
}
