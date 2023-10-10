package aop.stage0;

import aop.DataAccessException;
import aop.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

public class TransactionHandler implements InvocationHandler {

    private final Object target;
    private final PlatformTransactionManager platformTransactionManager;

    public TransactionHandler(final Object target, final PlatformTransactionManager platformTransactionManager) {
        this.target = target;
        this.platformTransactionManager = platformTransactionManager;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final Method declaredMethod = target.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
        if (declaredMethod.isAnnotationPresent(Transactional.class)) {
            return invokeWithTransactional(declaredMethod, args);
        }
        return declaredMethod.invoke(target, args);
    }

    private Object invokeWithTransactional(final Method method, final Object[] args) {
        final TransactionStatus status = platformTransactionManager.getTransaction(new DefaultTransactionAttribute());
        try {
            final Object result = method.invoke(target, args);
            platformTransactionManager.commit(status);
            return result;
        } catch (final Exception e) {
            platformTransactionManager.rollback(status);
            throw new DataAccessException(e);
        }
    }
}
