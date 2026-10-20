package aop.stage0;

import aop.DataAccessException;
import aop.service.UserService;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionHandler implements InvocationHandler {

    private final UserService userService;
    private final PlatformTransactionManager platformTransactionManager;

    public TransactionHandler(UserService userService, PlatformTransactionManager platformTransactionManager) {
        this.userService = userService;
        this.platformTransactionManager = platformTransactionManager;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        TransactionStatus transactionStatus = platformTransactionManager.getTransaction(
                new DefaultTransactionDefinition());
        try {
            Object invoke = method.invoke(userService, args);
            platformTransactionManager.commit(transactionStatus);
            return invoke;
        } catch (InvocationTargetException e){
            platformTransactionManager.rollback(transactionStatus);
            throw e.getTargetException();
        }
    }
}
