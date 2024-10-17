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

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */

    private final PlatformTransactionManager transactionManager;
    private final Object target;

    public TransactionHandler(PlatformTransactionManager transactionManager, Object target) {
        this.transactionManager = transactionManager;
        this.target = target;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        Method targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());

        if (targetMethod.isAnnotationPresent(Transactional.class)) {
            return invokeWithTransaction(method, args);
        }
        return method.invoke(target, args);
    }

    private Object invokeWithTransaction(Method method, Object[] args) throws Throwable {
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Object result = method.invoke(target, args);
            transactionManager.commit(transactionStatus);
            return result;
        } catch (InvocationTargetException e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException("트랜잭션 수행 중 예외가 발생해 롤백합니다.", e);
        }
    }
}
