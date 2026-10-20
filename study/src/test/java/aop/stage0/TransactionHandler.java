package aop.stage0;

import aop.DataAccessException;
import aop.Transactional;
import aop.service.AppUserService;
import aop.service.UserService;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionHandler implements InvocationHandler {

    private final AppUserService userService;
    private final PlatformTransactionManager platformTransactionManager;

    public TransactionHandler(final AppUserService userService, final PlatformTransactionManager platformTransactionManager) {
        this.userService = userService;
        this.platformTransactionManager = platformTransactionManager;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (!isTransactionalActivated(method)) {
            return method.invoke(userService, args);
        }

        final TransactionStatus transactionStatus =
            platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
        Object ref;

        try {
            ref = method.invoke(userService, args);
            platformTransactionManager.commit(transactionStatus);
            return ref;
        } catch (InvocationTargetException e) {
            platformTransactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
    }

    private boolean isTransactionalActivated(final Method method)
        throws NoSuchMethodException {
        final Method declaredMethod = userService.getClass()
            .getDeclaredMethod(method.getName(), method.getParameterTypes());

        return declaredMethod.isAnnotationPresent(Transactional.class);
    }
}
