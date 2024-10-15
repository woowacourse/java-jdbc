package aop.stage1;

import aop.DataAccessException;
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

    private final PlatformTransactionManager platformTransactionManager;
    private final Object target;

    public TransactionAdvice(PlatformTransactionManager platformTransactionManager, Object target) {
        this.platformTransactionManager = platformTransactionManager;
        this.target = target;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        TransactionStatus transactionStatus = platformTransactionManager.getTransaction(
                new DefaultTransactionDefinition());
        try {
            Method method = invocation.getMethod();
            Object[] args = invocation.getArguments();
            Object ret = method.invoke(target, args);
            platformTransactionManager.commit(transactionStatus);
            return ret;
        } catch (Exception e) {
            platformTransactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
    }
}
