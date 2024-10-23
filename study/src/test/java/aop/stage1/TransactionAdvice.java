package aop.stage1;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import aop.DataAccessException;

/**
 * 어드바이스(advice). 부가기능을 담고 있는 클래스
 */
public class TransactionAdvice  implements MethodInterceptor {

    private final PlatformTransactionManager transactionManager;

    public TransactionAdvice(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final var transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            final var target = invocation.getThis();
            final var method = invocation.getMethod();
            final var arguments = invocation.getArguments();
            final var result = method.invoke(target, arguments);
            invocation.proceed();
            transactionManager.commit(transaction);
            return result;
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new DataAccessException(e);
        }
    }
}
