package aop.stage0;

import aop.DataAccessException;
import aop.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionHandler implements InvocationHandler {

    private final PlatformTransactionManager transactionManager;
    private final Object target;

    public TransactionHandler(PlatformTransactionManager transactionManager, Object target) {
        this.transactionManager = transactionManager;
        this.target = target;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        Method targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
        if (targetMethod.isAnnotationPresent(Transactional.class)) {
            return invokeInTransaction(target, method, args);
        }

        return method.invoke(target, args);
    }

    private Object invokeInTransaction(Object proxy, Method method, Object[] args) {
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            Object data = method.invoke(proxy, args);
            transactionManager.commit(transactionStatus);
            return data;
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
    }
}
