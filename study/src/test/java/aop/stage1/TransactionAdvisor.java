package aop.stage1;

import org.aopalliance.aop.Advice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;

/**
 * 어드바이저(advisor). 포인트컷과 어드바이스를 하나씩 갖고 있는 객체.
 * AOP의 애스팩트(aspect)에 해당되는 클래스다.
 */
public class TransactionAdvisor implements PointcutAdvisor {
    private final Pointcut pointCut;
    private final Advice advice;

    public TransactionAdvisor(Pointcut pointCut, Advice advice) {
        this.pointCut = pointCut;
        this.advice = advice;
    }

    @Override
    public Pointcut getPointcut() {
        return pointCut;
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
