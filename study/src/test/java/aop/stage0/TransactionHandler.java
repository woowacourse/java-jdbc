package aop.stage0;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import aop.DataAccessException;
import aop.Transactional;

public class TransactionHandler implements InvocationHandler {

    private final PlatformTransactionManager transactionManager;
    private final Object target;

    public TransactionHandler(PlatformTransactionManager transactionManager, Object target) {
        this.transactionManager = transactionManager;
        this.target = target;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        var targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
        if (targetMethod.isAnnotationPresent(Transactional.class)) {
            var transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());
            try {
                var result = method.invoke(target, args);
                transactionManager.commit(transaction);
                return result;
            } catch (Exception e) {
                transactionManager.rollback(transaction);
                throw new DataAccessException(e);
            }
        }
        return method.invoke(target, args);
    }
}
