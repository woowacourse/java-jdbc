package aop.stage0;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import aop.Transactional;

public class TransactionHandler implements InvocationHandler {

    private final PlatformTransactionManager transactionManager;
    private final Object target;

    public TransactionHandler(PlatformTransactionManager transactionManager, Object target) {
        this.transactionManager = transactionManager;
        this.target = target;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (isNotTransactionMethod(method)) {
            return method.invoke(target, args);
        }

        final TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            final Object result = method.invoke(target, args);
            transactionManager.commit(transactionStatus);
            return result;
        } catch (InvocationTargetException e) {
            transactionManager.rollback(transactionStatus);
            throw e.getTargetException();
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw e;
        }
    }

    private boolean isNotTransactionMethod(Method method) {
        return method.getAnnotation(Transactional.class) == null;
    }
}
