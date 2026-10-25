package aop.stage1;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionAdvice implements MethodInterceptor {

    private final PlatformTransactionManager transactionManager;

    public TransactionAdvice(
            final PlatformTransactionManager transactionManager
    ) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final var status = transactionManager.getTransaction(
                new DefaultTransactionDefinition()
        );

        Object result;
        try {
            result = invocation.proceed();
        } catch (RuntimeException | Error e) {
            transactionManager.rollback(status);
            throw e;
        }

        transactionManager.commit(status);
        return result;
    }
}
