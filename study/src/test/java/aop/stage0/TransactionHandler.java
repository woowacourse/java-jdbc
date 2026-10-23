package aop.stage0;

import aop.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TransactionHandler implements InvocationHandler {

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */

    private final Object target;
    private final PlatformTransactionManager transactionManager;

    public TransactionHandler(final Object target, final PlatformTransactionManager transactionManager) {
        this.target = target;
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final Method targetMethod = target.getClass()
                .getMethod(method.getName(), method.getParameterTypes());

        final TransactionStatus status = targetMethod.isAnnotationPresent(Transactional.class)
                ? transactionManager.getTransaction(new DefaultTransactionDefinition())
                : null;

        final Object result;
        try {
            result = targetMethod.invoke(target, args);
        } catch (Throwable e) {
            if (status != null) {
                transactionManager.rollback(status);
            }

            if (e instanceof InvocationTargetException) {
                throw e.getCause();
            }
            throw e;
        }

        if (status != null) {
            transactionManager.commit(status);
        }
        return result;
    }
}
