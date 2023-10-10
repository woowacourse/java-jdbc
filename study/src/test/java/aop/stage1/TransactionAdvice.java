package aop.stage1;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

/**
 * 어드바이스(advice). 부가기능을 담고 있는 클래스
 */
public class TransactionAdvice implements MethodInterceptor {

    private final PlatformTransactionManager platformTransactionManager;

    public TransactionAdvice(final PlatformTransactionManager platformTransactionManager) {
        this.platformTransactionManager = platformTransactionManager;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final TransactionStatus status = platformTransactionManager.getTransaction(new DefaultTransactionAttribute());
        try {
            final Object result = invocation.proceed();
            platformTransactionManager.commit(status);
            return result;
        } catch (final Exception e) {
            platformTransactionManager.rollback(status);
            throw e;
        }
    }
}
