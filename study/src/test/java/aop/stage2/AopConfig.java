package aop.stage2;

import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import aop.stage1.TransactionAdvice;
import aop.stage1.TransactionAdvisor;
import aop.stage1.TransactionPointcut;

/**
 * 프록시 생성에 사용하는 클래스를 DI에 스프링 빈으로 등록하여 자동으로 프록시가 생성되도록!
 */
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
    public TransactionAdvisor transactionAdvisor(TransactionPointcut pointcut, TransactionAdvice advice) {
        return new TransactionAdvisor(pointcut, advice);
    }

    /**
     * DefaultAdvisorAutoProxyCreator: Spring AOP에서 제공하는 빈 후처리기
     * - 스프링 컨텍스트에 등록된 advisor를 자동으로 탐색하여 어드바이스를 적용할 타겟 빈에 프록시 객체를 생성해주는 역할
     * 덕분에 ProxyFactoryBean과 같은 객체를 별도로 설정하지 않아도, 스프링이 자동으로 프록시를 생성하여 어드바이스를 적용
     * (개발자가 수동으로 프록시 생성/설정할 필요 없이 어드바이저와 어드바이스를 관리할 수 있다!)
     */
    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        defaultAdvisorAutoProxyCreator.setProxyTargetClass(true); // CGLIB
        return defaultAdvisorAutoProxyCreator;
    }
}
