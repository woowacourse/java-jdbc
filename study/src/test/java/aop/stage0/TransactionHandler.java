
package aop.stage0;

import aop.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionHandler implements InvocationHandler {

    private final Object target;
    private final PlatformTransactionManager transactionManager;

    public TransactionHandler(
            final Object target,
            final PlatformTransactionManager transactionManager
    ) {
        this.target = target;
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        Method targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
        boolean transactional =
                targetMethod.isAnnotationPresent(Transactional.class)
                        || method.isAnnotationPresent(Transactional.class)
                        || target.getClass().isAnnotationPresent(Transactional.class);

        if (!transactional) {
            return invokeTarget(targetMethod, args);
        }

        final var status = transactionManager.getTransaction(
                new DefaultTransactionDefinition()
        );

        try {
            Object result = invokeTarget(targetMethod, args);
            transactionManager.commit(status);
            return result;
        } catch (RuntimeException | Error e) {
            if (!status.isCompleted()) {
                transactionManager.rollback(status);
            }
            throw e;
        }
    }

    private Object invokeTarget(Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }
}
