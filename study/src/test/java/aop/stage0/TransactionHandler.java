package aop.stage0;

import aop.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import transaction.stage1.jdbc.DataAccessException;

public class TransactionHandler implements InvocationHandler {

    private final PlatformTransactionManager transactionManager;

    public TransactionHandler(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (method.isAnnotationPresent(Transactional.class)) {
            final TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
            return invokeTransactionalMethod(proxy, method, args, status);
        }
        return method.invoke(transactionManager, args);
    }

    private Object invokeTransactionalMethod(Object proxy, Method method, Object[] args, TransactionStatus status) {
        try {
            final var result = method.invoke(proxy, args);
            transactionManager.commit(status);
            return result;

        } catch (final Exception e) {
            transactionManager.rollback(status);
            throw new DataAccessException(e);
        }
    }
}
