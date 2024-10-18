package aop.stage2;

import aop.stage1.TransactionAdvice;
import aop.stage1.TransactionAdvisor;
import aop.stage1.TransactionPointcut;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AopConfig {

    @Autowired
    PlatformTransactionManager transactionManager;

    @Bean
    Pointcut pointcut() {
        return new TransactionPointcut();
    }

    @Bean
    Advice advice() {
        return new TransactionAdvice(transactionManager);
    }

    @Bean
    Advisor advisor() {
        return new TransactionAdvisor(pointcut(), advice());
    }

//    @Bean //서비스에만 proxy 설정을 넣어주는 customize bean processor
//    BeanPostProcessor beanPostProcessor() {
//        return new CustomProxyBeanProcessor(advisor());
//    }

    @Bean
    DefaultAdvisorAutoProxyCreator proxyCreator() {
        DefaultAdvisorAutoProxyCreator proxyCreator = new DefaultAdvisorAutoProxyCreator();
        proxyCreator.setProxyTargetClass(true);
        return proxyCreator;
    }
}
