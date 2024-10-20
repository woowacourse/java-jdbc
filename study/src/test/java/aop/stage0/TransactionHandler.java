package aop.stage0;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import aop.DataAccessException;
import aop.Transactional;

public class TransactionHandler implements InvocationHandler {

    private final PlatformTransactionManager transactionManager;
    private final Object target;

    public TransactionHandler(final PlatformTransactionManager transactionManager, final Object target) {
        this.transactionManager = transactionManager;
        this.target = target;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        Method targetMethod = target.getClass()
                .getMethod(method.getName(), method.getParameterTypes());

        TransactionStatus status = null;
        Object result = null;
        if (!targetMethod.isAnnotationPresent(Transactional.class)) {
            return method.invoke(target, args);
        }
        try {
            status = transactionManager.getTransaction(TransactionDefinition.withDefaults());
            result = targetMethod.invoke(target, args);
            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw new DataAccessException(e);
        }
        return result;
    }
}
