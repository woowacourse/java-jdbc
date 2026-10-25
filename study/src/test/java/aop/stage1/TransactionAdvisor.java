
package aop.stage1;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;

public class TransactionAdvisor implements PointcutAdvisor {

    private final Pointcut pointcut;
    private final Advice advice;

    public TransactionAdvisor(
            final Pointcut pointcut,
            final Advice advice
    ) {
        this.pointcut = pointcut;
        this.advice = advice;
    }

    @Override
    public Pointcut getPointcut() {
        return pointcut;
    }

    @Override
    public Advice getAdvice() {
        return advice;
    }

    @Override
    public boolean isPerInstance() {
        return true;
    }
}
