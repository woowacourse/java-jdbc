package aop.stage0;

import aop.DataAccessException;
import aop.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

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
        if (isNotExistsTransaction(method, args)) {
            return method.invoke(target, args);
        }
        TransactionStatus transactionStatus = platformTransactionManager.getTransaction(
                new DefaultTransactionDefinition());
        Object result = null;
        try {
            result = method.invoke(target, args);
            platformTransactionManager.commit(transactionStatus);
        } catch (InvocationTargetException e) {
            platformTransactionManager.rollback(transactionStatus);
            throw new DataAccessException(e.getTargetException());
        }
        return result;
    }

    private boolean isNotExistsTransaction(Method method, Object[] args) throws NoSuchMethodException {
        Method targerMethod = target.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
        return !targerMethod.isAnnotationPresent(Transactional.class);
    }

}
