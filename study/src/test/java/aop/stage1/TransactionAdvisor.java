package aop.stage1;

import org.springframework.aop.PointcutAdvisor;

/**
 * 어드바이저(advisor). 포인트컷과 어드바이스를 하나씩 갖고 있는 객체.
 * AOP의 애스팩트(aspect)에 해당되는 클래스다.
 */
public class TransactionAdvisor implements PointcutAdvisor {

    private final TransactionAdvice advice;
    private final TransactionPointcut pointcut;

    public TransactionAdvisor(TransactionAdvice advice, TransactionPointcut pointcut) {
        this.advice = advice;
        this.pointcut = pointcut;
    }

    @Override
    public TransactionPointcut getPointcut() {
        return pointcut;
    }

    @Override
    public TransactionAdvice getAdvice() {
        return advice;
    }

    @Override
    public boolean isPerInstance() {
        return false;
    }
}
