package aop.stage2;

import aop.stage1.TransactionAdvice;
import aop.stage1.TransactionAdvisor;
import aop.stage1.TransactionPointcut;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AopConfig {

    private final PlatformTransactionManager platformTransactionManager;

    public AopConfig(PlatformTransactionManager platformTransactionManager) {
        this.platformTransactionManager = platformTransactionManager;
    }

    @Bean
    public Pointcut pointcut() {
        return new TransactionPointcut();
    }

    @Bean
    public Advice advice() {
        return new TransactionAdvice(platformTransactionManager);
    }

    @Bean
    public Advisor advisor() {
        return new TransactionAdvisor(pointcut(), advice());
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        // 이 친구가 proxyfactorybean을 자동생성해주는 역할을 한다.
        // 등록한 pointcut 조건에 맞춰 advice에 등록된 invoke 메서드를 실행한다.
        DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        // targetClass를 true로 설정해줘야 인터페이스를 따로 찾지 않고 구현한 targetClass에 직접 aop가 붙는다.
        defaultAdvisorAutoProxyCreator.setProxyTargetClass(true);
        return defaultAdvisorAutoProxyCreator;
    }
}
