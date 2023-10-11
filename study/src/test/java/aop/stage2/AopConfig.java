package aop.stage2;

import aop.Transactional;
import aop.stage1.TransactionAdvice;
import aop.stage1.TransactionAdvisor;
import aop.stage1.TransactionPointcut;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.Method;

@Configuration
public class AopConfig {
    @Autowired
    PlatformTransactionManager platformTransactionManager;

    @Bean
    TransactionPointcut transactionPointcut() {
        return new TransactionPointcut() {
            @Override
            public boolean matches(final Method method, final Class<?> targetClass) {
                return targetClass.isAnnotationPresent(Service.class) && method.isAnnotationPresent(Transactional.class);
            }
        };
    }

    @Bean
    TransactionAdvice transactionAdvice() {
        return new TransactionAdvice(platformTransactionManager);
    }

    @Bean
    TransactionAdvisor transactionAdvisor() {
        return new TransactionAdvisor(transactionPointcut(), transactionAdvice());
    }

    @Bean
    DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        return new DefaultAdvisorAutoProxyCreator();
    }
}
