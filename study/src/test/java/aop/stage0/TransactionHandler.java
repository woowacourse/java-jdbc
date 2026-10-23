package aop.stage0;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TransactionHandler implements InvocationHandler {

    private final PlatformTransactionManager platformTransactionManager;
    private final Object target;

    public TransactionHandler(PlatformTransactionManager platformTransactionManager, Object target) {
        this.platformTransactionManager = platformTransactionManager;
        this.target = target;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final TransactionStatus status = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            final var ret = method.invoke(target, args);
            platformTransactionManager.commit(status);
            return ret;
        } catch (final InvocationTargetException e) {
            platformTransactionManager.rollback(status);
            throw e.getTargetException();
        } catch (final RuntimeException | Error e) {
            platformTransactionManager.rollback(status);
            throw e;
        }
    }
}
