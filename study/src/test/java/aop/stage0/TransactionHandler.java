package aop.stage0;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TransactionHandler implements InvocationHandler {

    private final PlatformTransactionManager platformTransactionManager;
    private final Object target;

    public TransactionHandler(PlatformTransactionManager platformTransactionManager, Object target) {
        this.platformTransactionManager = platformTransactionManager;
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
        TransactionStatus status = platformTransactionManager.getTransaction(TransactionDefinition.withDefaults());
        try {
            proxy = method.invoke(target, args);
            platformTransactionManager.commit(status);
            return proxy;
        } catch (InvocationTargetException e) {
            platformTransactionManager.rollback(status);
            throw e.getCause();
        } catch (Throwable throwable) {
            platformTransactionManager.rollback(status);
            throw throwable;
        }
    }
}
