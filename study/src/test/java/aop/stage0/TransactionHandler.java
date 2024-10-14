package aop.stage0;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.springframework.data.util.ReflectionUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import aop.DataAccessException;
import aop.Transactional;

public class TransactionHandler implements InvocationHandler {

    private PlatformTransactionManager transactionManager;
    private Object target;

    public TransactionHandler(PlatformTransactionManager transactionManager, Object target) {
        this.transactionManager = transactionManager;
        this.target = target;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        Method targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
        if (targetMethod.isAnnotationPresent(Transactional.class)) {
            return executeWithTransaction(targetMethod, args);
        }
        return targetMethod.invoke(target, args);
    }

    private Object executeWithTransaction(Method method, Object[] args) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Object result = method.invoke(target, args);
            transactionManager.commit(status);
            return result;
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw new DataAccessException();
        }
    }
}
