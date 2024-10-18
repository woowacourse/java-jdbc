package aop.stage0;

import aop.DataAccessException;
import aop.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.jetbrains.annotations.NotNull;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

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
        Method targetMethod = getTargetMethod(target, method);

        if (!targetMethod.isAnnotationPresent(Transactional.class)) {
            return method.invoke(target, args);
        }

        return invokeMethodWithTranaction(method, args);
    }

    private Object invokeMethodWithTranaction(Method method, Object[] args) {
        Object result = null;
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            result = method.invoke(target, args);
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw new DataAccessException(e);
        }
        transactionManager.commit(status);
        return result;
    }


    private @NotNull Method getTargetMethod(Object target, Method method) throws NoSuchMethodException {
        return target.getClass()
                .getMethod(method.getName(), method.getParameterTypes());
    }
}
