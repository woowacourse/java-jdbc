package aop.stage0;

import aop.DataAccessException;
import aop.service.AppUserService;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

public class TransactionHandler implements InvocationHandler {

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    private final PlatformTransactionManager platformTransactionManager;

    private final AppUserService appUserService;

    public TransactionHandler(final PlatformTransactionManager platformTransactionManager,
                              final AppUserService appUserService) {
        this.platformTransactionManager = platformTransactionManager;
        this.appUserService = appUserService;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final TransactionStatus transaction = platformTransactionManager.getTransaction(null);
        try {
            final Object result = method.invoke(appUserService, args);
            platformTransactionManager.commit(transaction);
            return result;
        } catch (Exception e) {
            platformTransactionManager.rollback(transaction);
            throw new DataAccessException(e);
        }
    }
}
