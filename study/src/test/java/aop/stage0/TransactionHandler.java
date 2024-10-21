package aop.stage0;

import aop.DataAccessException;
import aop.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionHandler implements InvocationHandler {

    private final Object target;
    private final PlatformTransactionManager transactionManager;

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    public TransactionHandler(Object target, PlatformTransactionManager transactionManager) {
        this.target = target;
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        Method targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
        
        if (!targetMethod.isAnnotationPresent(Transactional.class)) {
            return targetMethod.invoke(target, args);
        }

        TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Object result = targetMethod.invoke(target, args);
            transactionManager.commit(transaction);
            return result;
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new DataAccessException(e);
        }
    }
}
