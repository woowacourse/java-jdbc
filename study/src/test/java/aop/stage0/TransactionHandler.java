package aop.stage0;

import aop.DataAccessException;
import aop.service.AppUserService;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

public class TransactionHandler implements InvocationHandler {

    private final PlatformTransactionManager transactionManager;
    private final AppUserService target;

    public TransactionHandler(PlatformTransactionManager transactionManager, AppUserService target) {
        this.transactionManager = transactionManager;
        this.target = target;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        TransactionStatus status = transactionManager.getTransaction(TransactionDefinition.withDefaults());

        try {
            Object result = method.invoke(target, args);
            transactionManager.commit(status);
            return result;
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw new DataAccessException();
        }
    }
}
