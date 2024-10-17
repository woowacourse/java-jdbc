package aop.stage0;

import aop.DataAccessException;
import aop.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionHandler implements InvocationHandler {

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    private final Object target;
    private final PlatformTransactionManager transactionManager;

    public TransactionHandler(Object target, PlatformTransactionManager transactionManager) {
        this.target = target;
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (target.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes()).isAnnotationPresent(Transactional.class)) {
            TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
            try {
                Object result = method.invoke(target, args);
                transactionManager.commit(status);
                return result;
            } catch (Exception e) {
                transactionManager.rollback(status);
                throw new DataAccessException(e.getMessage(), e);
            }
        } else {
            try {
                return method.invoke(target, args);
            } catch (Exception e) {
                throw new DataAccessException(e.getMessage(), e);
            }
        }
    }
}
