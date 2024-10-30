package aop.stage0;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import aop.Transactional;

public class TransactionHandler implements InvocationHandler {

    private final Object target;
    private final PlatformTransactionManager platformTransactionManager;

    public TransactionHandler(Object target, PlatformTransactionManager platformTransactionManager) {
        this.target = target;
        this.platformTransactionManager = platformTransactionManager;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (method.isAnnotationPresent(Transactional.class)) {
            return method.invoke(target, args);
        }

        TransactionStatus status = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Object result = method.invoke(target, args);
            platformTransactionManager.commit(status);
            return result;
        } catch (Exception e) {
            platformTransactionManager.rollback(status);
            throw e.getCause() != null ? e.getCause() : e;
        }
    }
}
