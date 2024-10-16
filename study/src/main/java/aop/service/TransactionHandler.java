package aop.service;

import aop.DataAccessException;
import aop.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class TransactionHandler implements InvocationHandler {

    private final Object target;
    private final PlatformTransactionManager transactionManager;

    public TransactionHandler(Object target, final PlatformTransactionManager transactionManager) {
        this.target = target;
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (method.isAnnotationPresent(Transactional.class)) {
            return invokeWithTransaction(method, args);
        }
        return method.invoke(target, args);
    }

    private Object invokeWithTransaction(final Method method, final Object[] args) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Object result = method.invoke(target, args);
            transactionManager.commit(status);
            return result;
        } catch (Exception exception) {
            transactionManager.rollback(status);
            throw new DataAccessException("트랜잭션을 커밋하는 데 실패하였습니다. 트랜잭션을 롤백합니다.");
        }
    }
}
