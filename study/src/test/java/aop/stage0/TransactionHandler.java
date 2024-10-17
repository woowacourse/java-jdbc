package aop.stage0;

import aop.DataAccessException;
import aop.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionHandler implements InvocationHandler {

    private final Object target;
    private final PlatformTransactionManager transactionManager;

    public TransactionHandler(Object target, PlatformTransactionManager transactionManager) {
        this.target = target;
        this.transactionManager = transactionManager;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        Method targetMethod = getTargetMethod(method);
        if (!targetMethod.isAnnotationPresent(Transactional.class)) {
            return method.invoke(target, args);
        }

        TransactionStatus transaction = startTransaction();
        try {
            Object result = method.invoke(target, args);
            transactionManager.commit(transaction);
            return result;
        } catch (Exception e) {
            transactionManager.rollback(transaction);
            throw new DataAccessException(e.getMessage(), e);
        }
    }

    private Method getTargetMethod(Method method) {
        return Arrays.stream(target.getClass().getDeclaredMethods())
                .filter(m -> m.getName().equals(method.getName()))
                .filter(m -> Arrays.equals(m.getParameterTypes(), method.getParameterTypes()))
                .findFirst()
                .orElseThrow(IllegalAccessError::new);
    }

    private TransactionStatus startTransaction() {
        return transactionManager.getTransaction(new DefaultTransactionDefinition());
    }
}
