package aop.stage0;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import aop.DataAccessException;
import aop.Transactional;

public class TransactionHandler implements InvocationHandler {

    private final PlatformTransactionManager platformTransactionManager;
    private final Object target;

    public TransactionHandler(final PlatformTransactionManager platformTransactionManager, final Object target) {
        this.platformTransactionManager = platformTransactionManager;
        this.target = target;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */


    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        Method declaredMethod = target.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
        if (declaredMethod.isAnnotationPresent(Transactional.class)) {
            return invokeWithTransaction(method, args);
        }
        return method.invoke(target, args);
    }

    private Object invokeWithTransaction(Method method, Object[] args) {
        TransactionStatus status = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Object result = method.invoke(target, args);
            platformTransactionManager.commit(status);
            return result;
        } catch (InvocationTargetException | IllegalAccessException | RuntimeException e) {
            platformTransactionManager.rollback(status);
            throw new DataAccessException(e);
        }
    }
}
