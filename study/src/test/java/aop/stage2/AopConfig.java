package aop.stage2;

import aop.stage1.TransactionAdvice;
import aop.stage1.TransactionAdvisor;
import aop.stage1.TransactionPointcut;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class AopConfig {

    @Bean
    public TransactionAdvisor transactionAdvisor(final PlatformTransactionManager transactionManager) {
        final TransactionAdvice transactionAdvice = new TransactionAdvice(transactionManager);
        final TransactionPointcut transactionPointcut = new TransactionPointcut();
        return new TransactionAdvisor(transactionAdvice, transactionPointcut);
    }

    // 주석을 풀면 에러가 발생한다.
    //org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'aop.stage2.Stage2Test': Unsatisfied dependency expressed through field 'userService':
    // Bean named 'userService' is expected to be of type 'aop.stage2.UserService' but was actually of type 'jdk.proxy3.$Proxy102'
//    @Bean
//    public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
//        return new DefaultAdvisorAutoProxyCreator();
//    }

}
