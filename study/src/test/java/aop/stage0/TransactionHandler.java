package aop.stage0;

import aop.DataAccessException;
import aop.Transactional;
import aop.service.AppUserService;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

public class TransactionHandler implements InvocationHandler {

    private final PlatformTransactionManager platformTransactionManager;
    private final AppUserService appUserService;

    public TransactionHandler(PlatformTransactionManager platformTransactionManager, AppUserService appUserService) {
        this.platformTransactionManager = platformTransactionManager;
        this.appUserService = appUserService;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) {
        if (hasMethodAnnotation(method)) {
            return invokeInTransaction(() -> invoke(method, args));
        }
        return invoke(method, args);
    }

    private boolean hasMethodAnnotation(Method method) {
        try {
            Method implementationMethod = appUserService.getClass().getMethod(method.getName(), method.getParameterTypes());
            return implementationMethod.isAnnotationPresent(Transactional.class);
        } catch (NoSuchMethodException e) {
            throw new DataAccessException(e);
        }
    }

    private Object invokeInTransaction(Callable<Object> callable) {
        TransactionStatus status = platformTransactionManager.getTransaction(TransactionDefinition.withDefaults());
        try {
            Object result = callable.call();
            platformTransactionManager.commit(status);
            return result;
        } catch (Exception e) {
            platformTransactionManager.rollback(status);
            throw new DataAccessException("An error occurred in transaction", e);
        }
    }

    private Object invoke(Method method, Object[] args) {
        try {
            return method.invoke(appUserService, args);
        } catch (Exception e) {
            throw new DataAccessException("An error occurred during invoke method", e);
        }
    }
}
