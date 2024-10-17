package aop.stage0;

import aop.DataAccessException;
import aop.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

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
            return method.invoke(target, args);
        }
        TransactionStatus transactionStatus
                = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Object result = method.invoke(target, args);
            platformTransactionManager.commit(transactionStatus);
            return result;
        } catch (Exception e) {
            platformTransactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
    }
}
