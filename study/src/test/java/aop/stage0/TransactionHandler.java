package aop.stage0;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import aop.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;

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
        Method targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());

        if (targetMethod.isAnnotationPresent(Transactional.class)) {
            TransactionStatus status = platformTransactionManager.getTransaction(TransactionDefinition.withDefaults());

            try {
                Object result = method.invoke(target, args);
                platformTransactionManager.commit(status);
                return result;
            } catch (Exception e) {
                platformTransactionManager.rollback(status);
                throw new DataAccessException();
            }
        }
        return method.invoke(target, args);
    }
}
