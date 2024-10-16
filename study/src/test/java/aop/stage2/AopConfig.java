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

    @Bean
    public TransactionAdvice transactionAdvice(PlatformTransactionManager manager) {
        return new TransactionAdvice(manager);
    }

    @Bean
    public TransactionAdvisor transactionAdvisor(TransactionPointcut pointcut, TransactionAdvice advice) {
        return new TransactionAdvisor(pointcut, advice);
    }

    @Bean
    public TransactionPointcut transactionPointcut() {
        return new TransactionPointcut();
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        //프록시가 생성될 대상 클래스가 인터페이스를 구현하지 않는 경우, JDK 동적 프록시 방식으로는 프록시를 생성할 수 없음.
        //이럴 때 setProxyTargetClass(true)를 호출
        // 클래스 기반 프록시가 생성되어 오류가 발생하지 않음
        creator.setProxyTargetClass(true);
        return creator;
    }
}
