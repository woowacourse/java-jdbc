package aop.stage2;

import aop.Transactional;
import aop.stage1.TransactionAdvice;
import aop.stage1.TransactionAdvisor;
import java.lang.reflect.Method;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.aop.support.StaticMethodMatcherPointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AopConfig {

    @Autowired
    private PlatformTransactionManager platformTransactionManager;

    @Bean
    public TransactionAdvice transactionAdvice() {
        return new TransactionAdvice(platformTransactionManager);
    }

    @Bean
    public Pointcut transactionPointcut() {
        return new StaticMethodMatcherPointcut() {
            @Override
            public boolean matches(final Method method, final Class<?> targetClass) {
                return targetClass.isAnnotationPresent(Service.class) &&
                       method.isAnnotationPresent(Transactional.class);
            }
        };
    }

    @Bean
    public TransactionAdvisor transactionAdvisor() {
        return new TransactionAdvisor(transactionPointcut(), transactionAdvice());
    }

    @Bean
    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        return new DefaultAdvisorAutoProxyCreator();
    }
}
