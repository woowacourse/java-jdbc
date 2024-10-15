package aop.stage0;

import aop.DataAccessException;
import aop.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
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
            return targetMethod.invoke(target, args);
        }

        TransactionStatus status = platformTransactionManager.getTransaction(TransactionDefinition.withDefaults());
        try {
            Object result = targetMethod.invoke(target, args);
            platformTransactionManager.commit(status);
            return result;
        } catch (Exception e) {
            platformTransactionManager.rollback(status);
            throw new DataAccessException(e);
        }

    }
}
