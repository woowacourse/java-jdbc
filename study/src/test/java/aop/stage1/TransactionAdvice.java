package aop.stage1;

import aop.DataAccessException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Object invoked = null;
        Method method = invocation.getMethod();
        Object[] arguments = invocation.getArguments();
        Object target = invocation.getThis();
        try {
            invoked = method.invoke(target, arguments);
        } catch (InvocationTargetException e) {
            transactionManager.rollback(transactionStatus);
            throw e.getTargetException();
        } catch (RuntimeException e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
        transactionManager.commit(transactionStatus);
        return invoked;
    }
}
