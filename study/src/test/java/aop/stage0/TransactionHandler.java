package aop.stage0;

import aop.DataAccessException;
import aop.Transactional;
import aop.service.AppUserService;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
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
        validateHasMethodAnnotation(method);
        TransactionStatus status = platformTransactionManager.getTransaction(TransactionDefinition.withDefaults());
        try {
            Object result = method.invoke(appUserService, args);
            platformTransactionManager.commit(status);
            return result;
        } catch (Exception e) {
            platformTransactionManager.rollback(status);
            throw new DataAccessException("An error occurred during transaction", e);
        }
    }

    private void validateHasMethodAnnotation(Method method) {
        try {
            Method implementationMethod = appUserService.getClass().getMethod(method.getName(), method.getParameterTypes());
            if (!implementationMethod.isAnnotationPresent(Transactional.class)) {
                throw new DataAccessException("Method has not Transactional annotation");
            }
        } catch (NoSuchMethodException e) {
            throw new DataAccessException(e);
        }
    }
}
