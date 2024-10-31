package aop.stage0;

import aop.DataAccessException;
import aop.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionHandler implements InvocationHandler {

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    private final PlatformTransactionManager platformTransactionManager;
    private final Object target;

    public TransactionHandler(PlatformTransactionManager platformTransactionManager, Object target) {
        this.platformTransactionManager = platformTransactionManager;
        this.target = target;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) {
        if (isTransactionalMethod(method)) {
            return invokeWithTransaction(method, args);
        }
        return invokeWithoutTransaction(method, args);
    }

    private Object invokeWithTransaction(Method method, Object[] args) {
        final var transactionStatus = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());

        Object object = null;

        try {
            object = method.invoke(target, args);
        } catch (Exception e) {
            platformTransactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
        platformTransactionManager.commit(transactionStatus);
        return object;
    }

    private boolean isTransactionalMethod(Method method) {
        return method.isAnnotationPresent(Transactional.class);
    }

    private Object invokeWithoutTransaction(Method method, Object[] args) {
        try {
            return method.invoke(target, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
