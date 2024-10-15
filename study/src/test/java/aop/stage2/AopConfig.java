package aop.stage2;

import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import aop.stage1.TransactionAdvice;
import aop.stage1.TransactionAdvisor;
import aop.stage1.TransactionPointcut;

@Configuration
public class AopConfig {

    private final PlatformTransactionManager platformTransactionManager;

    public AopConfig(PlatformTransactionManager platformTransactionManager) {
        this.platformTransactionManager = platformTransactionManager;
    }

    @Bean
    public TransactionPointcut transactionPointcut() {
        return new TransactionPointcut();
    }

    @Bean
    public TransactionAdvice transactionAdvice() {
        return new TransactionAdvice(platformTransactionManager);
    }

    @Bean
    public TransactionAdvisor transactionAdvisor() {
        return new TransactionAdvisor(transactionPointcut(), transactionAdvice());
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        final DefaultAdvisorAutoProxyCreator proxyCreator = new DefaultAdvisorAutoProxyCreator();
        proxyCreator.setProxyTargetClass(true);

        return proxyCreator;
    }
}
