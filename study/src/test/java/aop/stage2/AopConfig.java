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
    public TransactionAdvisor transactionAdvisor(PlatformTransactionManager transactionManager) {
        final var pointcut = new TransactionPointcut();
        final var advice = new TransactionAdvice(transactionManager);
        return new TransactionAdvisor(pointcut, advice);
    }

    // 스프링 컨테이너에 등록된 모든 빈을 검사 -> 각 어드바이저의 포인트컷과 매칭되는 빈을 찾음 -> 자동으로 프록시를 생성하여 원본 빈을 대체
    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator creator = new DefaultAdvisorAutoProxyCreator();
        creator.setProxyTargetClass(true);  // CGLIB 프록시 사용, 사용하지 않을 시 JDK Proxy로 만들어져 인터페이스가 필수가 된다.
        return creator;
    }
}
