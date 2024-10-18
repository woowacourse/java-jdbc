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

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */

    private final PlatformTransactionManager transactionManager;
    private final Object proxyTarget;

    public TransactionHandler(PlatformTransactionManager transactionManager, Object proxyTarget) {
        this.transactionManager = transactionManager;
        this.proxyTarget = proxyTarget;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        Method declaredMethod = proxyTarget.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
        if (declaredMethod.isAnnotationPresent(Transactional.class)) {
            return executeTransaction(method, args);
        }
        return method.invoke(proxyTarget, args);
    }

    private Object executeTransaction(Method method, Object[] args) {
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Object result = method.invoke(proxyTarget, args);
            transactionManager.commit(transactionStatus);
            return result;

        } catch (IllegalAccessException | InvocationTargetException | RuntimeException e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
    }
}
