package aop.stage2;

import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import aop.stage1.TransactionAdvice;
import aop.stage1.TransactionAdvisor;

@Configuration
public class AopConfig {

    private final PlatformTransactionManager transactionManager;

    public AopConfig(final PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Bean
    public TransactionAdvice transactionAdvice() {
        return new TransactionAdvice(transactionManager);
    }

    @Bean
    public TransactionAdvisor transactionAdvisor() {
        return new TransactionAdvisor(transactionManager);
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        final DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);
        return creator;
    }
}
