package aop.stage0;

import aop.DataAccessException;
import aop.Transactional;
import aop.service.UserService;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TransactionHandler implements InvocationHandler {

    private final PlatformTransactionManager platformTransactionManager;

    private final UserService target;

    public TransactionHandler(PlatformTransactionManager platformTransactionManager, UserService userService) {
        this.platformTransactionManager = platformTransactionManager;
        this.target = userService;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (!method.getClass().isAnnotationPresent(Transactional.class)) {
            return method.invoke(target, args);
        }

        TransactionStatus transactionStatus = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Object result = method.invoke(target, args);
            platformTransactionManager.commit(transactionStatus);
            return result;
        } catch (IllegalAccessException | InvocationTargetException | TransactionException exception) {
            platformTransactionManager.rollback(transactionStatus);
            throw new DataAccessException();
        }
    }
}
