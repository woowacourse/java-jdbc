package aop.stage2;

import aop.stage1.TransactionAdvice;
import aop.stage1.TransactionAdvisor;
import aop.stage1.TransactionPointcut;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AopConfig {

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    protected Pointcut transactionalPointcut() {
        return new TransactionPointcut();
    }

    @Bean
    protected Advice transactionalAdvice() {
        return new TransactionAdvice(transactionManager);
    }

    @Bean
    public Advisor transactionalAdviser() {
        return new TransactionAdvisor(transactionalPointcut(), transactionalAdvice());
    }
}
