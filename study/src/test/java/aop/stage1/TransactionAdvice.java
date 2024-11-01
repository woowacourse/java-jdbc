package aop.stage1;

import aop.DataAccessException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

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
        TransactionStatus status = platformTransactionManager.getTransaction(TransactionDefinition.withDefaults());
        try {
            Object proceed = invocation.proceed();
            platformTransactionManager.commit(status);
            return proceed;
        } catch (Throwable e) {
            platformTransactionManager.rollback(status);
            throw new DataAccessException("exception occurred, rolling back..");
        }
    }
}
