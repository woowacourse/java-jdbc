package aop.stage1;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;

/**
 * 어드바이저(advisor). 포인트컷과 어드바이스를 하나씩 갖고 있는 객체.
 * AOP의 애스팩트(aspect)에 해당되는 클래스다.
 */
public class TransactionAdvisor implements PointcutAdvisor {

    private final Pointcut pointcut;
    private final Advice advice;

    public TransactionAdvisor(final Pointcut pointcut, final Advice advice) {
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

    /**
     * 현재 프레임워크에서 사용되지는 않는다.
     * @return true
     */
    @Override
    public boolean isPerInstance() {
        return true;
    }
}
