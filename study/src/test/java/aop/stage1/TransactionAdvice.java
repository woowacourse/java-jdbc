package aop.stage1;

import java.lang.reflect.InvocationTargetException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * 어드바이스(advice). 부가기능을 담고 있는 클래스
 */
public class TransactionAdvice implements MethodInterceptor {

    private final PlatformTransactionManager transactionManager;

    public TransactionAdvice(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Object result = invocation.proceed();
            transactionManager.commit(transactionStatus);
            return result;
        } catch (InvocationTargetException e) {
            transactionManager.rollback(transactionStatus);
            throw e.getTargetException();
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw e;
        }
    }
}
