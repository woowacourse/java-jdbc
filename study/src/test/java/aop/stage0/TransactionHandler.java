package aop.stage0;

import aop.Transactional;
import aop.service.AppUserService;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionHandler implements InvocationHandler {

    private static final Logger log = LoggerFactory.getLogger(TransactionHandler.class);
    private final PlatformTransactionManager platformTransactionManager;
    private final AppUserService appUserService;

    public TransactionHandler(final PlatformTransactionManager platformTransactionManager,
                              final AppUserService appUserService) {
        this.platformTransactionManager = platformTransactionManager;
        this.appUserService = appUserService;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

        final Method target = appUserService.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());

        if (!target.isAnnotationPresent(Transactional.class)) {
            try {
                return method.invoke(appUserService, args);
            } catch (final Exception e) {
                log.error("트랜잭션 적용 안됨: {}", method.getDeclaringClass());
                throw e;
            }
        }

        final TransactionStatus transaction =
                platformTransactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            final Object result = method.invoke(appUserService, args);
            platformTransactionManager.commit(transaction);
            return result;
        } catch (final Exception e) {
            platformTransactionManager.rollback(transaction);
            throw e.getCause();
        }
    }
}
