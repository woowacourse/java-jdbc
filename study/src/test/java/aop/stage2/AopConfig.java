package aop.stage2;

import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.transaction.PlatformTransactionManager;

import aop.stage1.TransactionAdvisor;

@Configuration
@EnableAspectJAutoProxy
public class AopConfig {

    @Bean
    public DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator proxyCreator = new DefaultAdvisorAutoProxyCreator();
        proxyCreator.setProxyTargetClass(true);  // CGLIB 기반 프록시 생성
        return proxyCreator;
    }

    @Bean
    public TransactionAdvisor transactionAdvisor(PlatformTransactionManager transactionManager) {
        return new TransactionAdvisor(transactionManager);
    }
}
