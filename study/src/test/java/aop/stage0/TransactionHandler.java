package aop.stage0;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import aop.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionHandler implements InvocationHandler {

    private final Object target;
    private final PlatformTransactionManager transactionManager;

    public TransactionHandler(Object target, PlatformTransactionManager transactionManager) {
        this.target = target;
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
        if (targetMethod.isAnnotationPresent(aop.Transactional.class)) {
            TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
            Object result = invokeWithTransaction(method, args, status);
            transactionManager.commit(status);
            return result;
        }

        return method.invoke(target, args);
    }

    private Object invokeWithTransaction(Method method, Object[] args, TransactionStatus status) {
        try {
            return method.invoke(target, args);
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw new DataAccessException(e);
        }
    }
}
