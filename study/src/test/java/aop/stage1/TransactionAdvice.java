package aop.stage1;

import aop.DataAccessException;
import aop.common.TransactionHandler;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.lang.reflect.Method;

/**
 * 어드바이스(advice). 부가기능을 담고 있는 클래스
 */
public class TransactionAdvice implements MethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TransactionHandler.class);

    private final PlatformTransactionManager transactionManager;

    public TransactionAdvice(final PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final Object target = invocation.getThis();
        final Method method = invocation.getMethod();

        return invokeWithTransaction(target, method, invocation.getArguments());
    }

    private Object invokeWithTransaction(final Object target, final Method method, final Object[] args) {
        final TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            log.info("Start Transaction for: {}", method.getName());
            final Object result = method.invoke(target, args);
            transactionManager.commit(transaction);
            log.info("End Transaction for: {}", method.getName());

            return result;
        } catch (final Exception e) {
            transactionManager.rollback(transaction);
            log.info("Errors occurred when committing: {}", method.getName());

            throw new DataAccessException(e);
        }
    }
}
