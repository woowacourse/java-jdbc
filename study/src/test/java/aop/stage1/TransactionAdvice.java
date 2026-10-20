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
    private final PlatformTransactionManager platformTransactionManager;

    public TransactionAdvice(PlatformTransactionManager platformTransactionManager) {
        this.platformTransactionManager = platformTransactionManager;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        TransactionStatus transactionStatus = platformTransactionManager.getTransaction(
                new DefaultTransactionDefinition());
        try {
            Object invoke = invocation.getMethod().invoke(invocation.getThis(), invocation.getArguments());
            platformTransactionManager.commit(transactionStatus);
            return invoke;
        } catch (InvocationTargetException e) {
            platformTransactionManager.rollback(transactionStatus);
            throw e.getTargetException();
        }
    }
}
