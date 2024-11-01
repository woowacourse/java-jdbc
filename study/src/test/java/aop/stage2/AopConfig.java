package aop.stage2;

import aop.stage1.TransactionAdvice;
import aop.stage1.TransactionAdvisor;
import aop.stage1.TransactionPointcut;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AopConfig {

    @Bean
    public TransactionAdvice transactionAdvice(PlatformTransactionManager platformTransactionManager) {
        return new TransactionAdvice(platformTransactionManager);
    }

    @Bean
    public PointcutAdvisor transactionPointcutAdvisor(TransactionPointcut transactionPointcut,
                                                      TransactionAdvice transactionAdvice) {
        return new TransactionAdvisor(transactionPointcut, transactionAdvice);
    }

    @Bean
    public TransactionPointcut transactionPointcut() {
        return new TransactionPointcut();
    }

}
