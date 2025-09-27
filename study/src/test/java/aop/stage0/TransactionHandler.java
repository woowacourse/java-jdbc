package aop.stage0;

import aop.DataAccessException;
import aop.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

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
        final Method target = this.target.getClass().getMethod(method.getName(), method.getParameterTypes());

        if (target.isAnnotationPresent(Transactional.class)) {
            return invokeInTransaction(method, args);
        } else {
            try {
                return method.invoke(this.target, args);
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
        }
    }

    private Object invokeInTransaction(final Method method, final Object[] args) throws Throwable {
        final var transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Object invoke = null;
        try {
            invoke = method.invoke(target, args);
            transactionManager.commit(transactionStatus);
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
        }
        return invoke;
    }
}
