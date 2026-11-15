package aop.stage0;

import aop.service.AppUserService;
import aop.service.UserService;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionHandler implements InvocationHandler {

    private final PlatformTransactionManager transactionManager;
    private final UserService userService;

    public TransactionHandler(
            final PlatformTransactionManager platformTransactionManager,
            final AppUserService appUserService
    ) {
        this.transactionManager = platformTransactionManager;
        this.userService = appUserService;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(
            final Object proxy,
            final Method method,
            final Object[] args
    ) throws Throwable {
        final var transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        final Object result;
        try {
            result = method.invoke(userService, args);
        } catch (InvocationTargetException e) {
            transactionManager.rollback(transactionStatus);
            throw e.getCause();
        }
        transactionManager.commit(transactionStatus);
        return result;
    }
}
