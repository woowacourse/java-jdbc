package aop.stage1;

import aop.DataAccessException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * MethodInterceptor처럼 부가기능을 담고 있는 클래스를 스프링에서 어드바이스라고 한다.
 */
public class TransactionAdvice implements MethodInterceptor {

    private final PlatformTransactionManager transactionManager;

    public TransactionAdvice(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    // 타깃을 호출하는 기능을 가진 콜백 오브젝트(MethodInvocation)를 프록시로부터 받는다.
    // 덕분에 어드바이스는 특정 타깃에 의존하지 않고 재사용 가능하다.
    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            // 콜백을 호출해서 타깃의 메소드를 실행한다.
            Object ret = invocation.proceed();
            transactionManager.commit(transactionStatus);
            return ret;
        } catch (RuntimeException e) {
            // JDK 다이나믹 프록시가 제공하는 Method와 달리
            // 스프링의 MethodInvocation을 통한 타깃 호출은 예외가 포장되지 않고 타깃에서 보낸 그대로 전달된다.
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
    }
}
