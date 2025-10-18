package aop.stage2;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AopConfig {

    @Bean
    public TransactionAspect transactionAdvice(PlatformTransactionManager platformTransactionManager) {
        return new TransactionAspect(platformTransactionManager);
    }
}
