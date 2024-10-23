package aop.stage0;

import aop.DataAccessException;
import aop.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

public class TransactionHandler implements InvocationHandler {

    private final PlatformTransactionManager platformTransactionManager;
    private final Object target;

    public TransactionHandler(PlatformTransactionManager platformTransactionManager, Object target) {
        this.platformTransactionManager = platformTransactionManager;
        this.target = target;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        Method targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());

        if (!targetMethod.isAnnotationPresent(Transactional.class)) {
            return invoke(args, targetMethod);
        }

        return invokeTransaction(() -> invoke(args, targetMethod));
    }

    private Object invokeTransaction(Callable<Object> callable) {
        TransactionStatus status = platformTransactionManager.getTransaction(TransactionDefinition.withDefaults());
        try {
            Object result = callable.call();
            platformTransactionManager.commit(status);
            return result;
        } catch (Exception e) {
            platformTransactionManager.rollback(status);
            throw new DataAccessException(e);
        }
    }

    private Object invoke(Object[] args, Method targetMethod) {
        try {
            return targetMethod.invoke(target, args);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException();
        }
    }
}
