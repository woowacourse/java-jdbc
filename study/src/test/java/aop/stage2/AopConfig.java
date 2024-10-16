package aop.stage2;

import aop.stage1.TransactionAdvice;
import aop.stage1.TransactionAdvisor;
import aop.stage1.TransactionPointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AopConfig {

    @Bean
    public PointcutAdvisor pointcutAdvisor(PlatformTransactionManager platformTransactionManager) {
        return new TransactionAdvisor(new TransactionPointcut(), new TransactionAdvice(platformTransactionManager));
    }
}
