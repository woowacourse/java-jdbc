package aop.stage0;

import aop.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class TransactionHandler implements InvocationHandler {

    private static final String TRANSACTION_FAIL_EXCEPTION = "Transaction을 실행하던 도중 실패했습니다.";

    private final PlatformTransactionManager transactionManager;

    private final Object target;

    public TransactionHandler(PlatformTransactionManager platformTransactionManager, Object clazz) {
        this.transactionManager = platformTransactionManager;
        this.target = clazz;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("transaction");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            Object result = method.invoke(target, args);
            transactionManager.commit(status);
            return result;
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw new DataAccessException(TRANSACTION_FAIL_EXCEPTION, e);
        }
    }
}
