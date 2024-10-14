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
        Method targetMethod = target.getClass()
                .getMethod(method.getName(), method.getParameterTypes());
        if (targetMethod.isAnnotationPresent(Transactional.class)) {
            return doTransaction(targetMethod, args);
        }
        return targetMethod.invoke(target, args);
    }

    private Object doTransaction(Method method, Object[] args) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Object result;
        try {
            result = method.invoke(target, args);
        } catch (Throwable e) {
            transactionManager.rollback(status);
            throw new DataAccessException(e);
        }
        transactionManager.commit(status);
        return result;
    }
}
