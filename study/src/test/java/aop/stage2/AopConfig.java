package aop.stage2;

import aop.stage1.TransactionAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AopConfig {

    @Bean
    public TransactionAdvisor transactionAdvisor(PlatformTransactionManager transactionManager) {
        return new TransactionAdvisor(transactionManager);
    }
}
