package aop.stage0;

import aop.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionHandler implements InvocationHandler {

    private final Object target;
    private final PlatformTransactionManager transactionManager;

    public TransactionHandler(final Object target,
            final PlatformTransactionManager transactionManager) {
        this.target = target;
        this.transactionManager = transactionManager;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] arguments)
            throws Throwable {
        final Method targetMethod = target.getClass()
                .getMethod(method.getName(), method.getParameterTypes());
        if (!targetMethod.isAnnotationPresent(Transactional.class)) {
            return invokeTarget(targetMethod, arguments);
        }

        final var transactionStatus = transactionManager.getTransaction(
                new DefaultTransactionDefinition());
        final Object result;
        try {
            result = invokeTarget(targetMethod, arguments);
        } catch (Throwable exception) {
            transactionManager.rollback(transactionStatus);
            throw exception;
        }
        transactionManager.commit(transactionStatus);
        return result;
    }

    private Object invokeTarget(final Method method, final Object[] arguments) throws Throwable {
        try {
            return method.invoke(target, arguments);
        } catch (InvocationTargetException exception) {
            throw exception.getCause();
        }
    }
}
