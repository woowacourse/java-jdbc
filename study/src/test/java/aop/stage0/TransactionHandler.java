package aop.stage0;

import aop.DataAccessException;
import aop.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionHandler implements InvocationHandler {

    private final Object target;
    private final PlatformTransactionManager transactionManager;

    public TransactionHandler(Object target, PlatformTransactionManager transactionManager) {
        this.target = target;
        this.transactionManager = transactionManager;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        Method realMethod = target.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
        if (realMethod.isAnnotationPresent(Transactional.class)) {
            TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
            Object object = invokeHandler(method, args, transactionStatus);
            transactionManager.commit(transactionStatus);
            return object;
        }
        return method.invoke(target, args);
    }

    private Object invokeHandler(
            Method method, Object[] args, TransactionStatus transactionStatus
    ) {
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
    }
}
