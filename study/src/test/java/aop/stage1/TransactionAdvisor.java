package aop.stage1;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 어드바이저(advisor). 포인트컷과 어드바이스를 하나씩 갖고 있는 객체.
 * AOP의 애스팩트(aspect)에 해당되는 클래스다.
 */
public class TransactionAdvisor implements PointcutAdvisor {

    private final PlatformTransactionManager platformTransactionManager;

    public TransactionAdvisor(PlatformTransactionManager platformTransactionManager) {
        this.platformTransactionManager = platformTransactionManager;
    }

    @Override
    public Pointcut getPointcut() {
        return new TransactionPointcut();
    }

    @Override
    public Advice getAdvice() {
        return new TransactionAdvice(platformTransactionManager);
    }

    @Override
    public boolean isPerInstance() {
        return true;
    }
}
