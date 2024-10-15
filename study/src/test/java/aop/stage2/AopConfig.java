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
    public TransactionPointcut transactionPointcut() {
        return new TransactionPointcut();
    }

    @Bean
    public TransactionAdvice transactionAdvice(PlatformTransactionManager platformTransactionManager) {
        return new TransactionAdvice(platformTransactionManager);
    }

    @Bean
    public TransactionAdvisor transactionAdvisor(
            TransactionPointcut transactionPointcut,
            TransactionAdvice transactionAdvice
    ) {
        return new TransactionAdvisor(transactionPointcut, transactionAdvice);
    }

    // DefaultAdvisorAutoProxyCreator는 조건에 맞는 빈에 대해 자동으로 프록시를 생성한다고 합니다.
    // 이 빈을 등록하면 예외가 발생해서 주석 처리했습니다.
    //
    // @Bean
    // public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
    //     return new DefaultAdvisorAutoProxyCreator();
    // }
}
