package aop.stage1;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import aop.DataAccessException;

/**
 * 어드바이스(advice). 부가기능을 담고 있는 클래스
 *
 * Advice: 무엇을 실행할지 정의. Pointcut으로 정의된 지점에서 실행된다.
 */
public class TransactionAdvice implements MethodInterceptor {

    private final PlatformTransactionManager transactionManager;

    public TransactionAdvice(final PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        TransactionStatus status = null;
        Object result = null;
        try {
            status = transactionManager.getTransaction(TransactionDefinition.withDefaults());
            result = invocation.proceed();
            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw new DataAccessException(e);
        }
        return result;
    }
}
