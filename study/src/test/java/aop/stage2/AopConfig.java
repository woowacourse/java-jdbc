package aop.stage2;

import aop.stage1.TransactionAdvice;
import aop.stage1.TransactionAdvisor;
import aop.stage1.TransactionPointcut;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AopConfig {

    private final PlatformTransactionManager transactionManager;

    public AopConfig(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Bean
    public TransactionAdvisor transactionAdvisor() {
        TransactionPointcut pointCut = new TransactionPointcut();
        TransactionAdvice advice = new TransactionAdvice(transactionManager);
        return new TransactionAdvisor(pointCut, advice);
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator proxyCreator = new DefaultAdvisorAutoProxyCreator();
        // 대상이 Interface면 JDK Proxy, Class면 CGLIB Proxy를 생성.
        proxyCreator.setProxyTargetClass(true);
        return proxyCreator;
    }
}
