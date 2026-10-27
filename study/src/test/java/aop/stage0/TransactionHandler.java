package aop.stage0;

import aop.service.UserService;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionHandler implements InvocationHandler {
    private final PlatformTransactionManager manager;
    private final Object target;

    public TransactionHandler(PlatformTransactionManager manager, Object target) {
        this.manager = manager;
        this.target = target;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();

        TransactionStatus transactionStatus = manager.getTransaction(transactionDefinition);
        Object invoked = null;
        try {
            invoked = method.invoke(target, args);
            manager.commit(transactionStatus);
        } catch (RuntimeException | Error e) {
            manager.rollback(transactionStatus);
        }
        return invoked;
    }
}
