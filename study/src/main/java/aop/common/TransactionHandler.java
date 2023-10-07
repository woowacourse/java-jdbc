package aop.common;

import aop.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class TransactionHandler implements InvocationHandler {

    private static final Logger log = LoggerFactory.getLogger(TransactionHandler.class);

    private final PlatformTransactionManager transactionManager;
    private final Object target;

    public TransactionHandler(final PlatformTransactionManager transactionManager, final Object target) {
        this.transactionManager = transactionManager;
        this.target = target;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final Method targetMethod = target.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
        
        if (targetMethod.isAnnotationPresent(Transactional.class)) {
            return invokeWithTransaction(target, method, args);
        }

        return method.invoke(target, args);
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
