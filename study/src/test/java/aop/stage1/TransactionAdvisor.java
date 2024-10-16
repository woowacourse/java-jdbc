package aop.stage1;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;

/**
 * 어드바이저(advisor). 포인트컷과 어드바이스를 하나씩 갖고 있는 객체.
 * AOP의 애스팩트(aspect)에 해당되는 클래스다.
 *
 * Advisor: 어떤 시점에서 어떤 로직을 실행해야 하는지 전체적으로 관리.
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

    @Override
    public boolean isPerInstance() {
        return false;
    }
}
