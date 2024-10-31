package aop.stage0;

import aop.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
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
        if (hasTransactionalAnnotation(method)) {
            return invokeWithTransaction(method, args);
        }

        return method.invoke(target, args);
    }

    private boolean hasTransactionalAnnotation(Method method) {
        return Arrays.stream(target.getClass().getDeclaredMethods())
                .filter(m -> m.getName().equals(method.getName()))
                .anyMatch(m -> m.isAnnotationPresent(Transactional.class));
    }

    private Object invokeWithTransaction(final Method method, final Object[] args) throws Throwable {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            final Object result = method.invoke(target, args);
            transactionManager.commit(status);
            return result;
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw e.getCause();
        }
    }
}
