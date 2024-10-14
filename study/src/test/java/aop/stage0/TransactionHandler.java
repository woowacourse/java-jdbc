package aop.stage0;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.SQLException;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import aop.DataAccessException;
import aop.service.AppUserService;

public class TransactionHandler implements InvocationHandler {

    private final PlatformTransactionManager transactionManager;
    private final AppUserService target;

    public TransactionHandler(PlatformTransactionManager platformTransactionManager, AppUserService target) {
        this.transactionManager = platformTransactionManager;
        this.target = target;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) {
        TransactionStatus status = transactionManager.getTransaction(TransactionDefinition.withDefaults());;
        Object result;
        try {
            result = method.invoke(target, args);
            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw new DataAccessException(e);
        }
        return result;
    }
}
