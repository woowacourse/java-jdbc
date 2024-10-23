package aop.stage0;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import aop.DataAccessException;
import aop.Transactional;

public class TransactionHandler implements InvocationHandler {

    private final Object target;
    private final PlatformTransactionManager transactionManager;

    public TransactionHandler(final Object target, final PlatformTransactionManager transactionManager) {
        this.target = target;
        this.transactionManager = transactionManager;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final Method targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
        if (!targetMethod.isAnnotationPresent(Transactional.class)) {
            return targetMethod.invoke(target, args);
        }

        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Object result;
        try {
            result = targetMethod.invoke(target, args);
            transactionManager.commit(status);
        } catch (final Exception e) {
            transactionManager.rollback(status);
            throw new DataAccessException();
        }

        return result;
    }
}
