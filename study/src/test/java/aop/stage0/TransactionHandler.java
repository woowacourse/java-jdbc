package aop.stage0;

import aop.DataAccessException;
import aop.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionHandler implements InvocationHandler {

    private final Object target;
    private final Map<String, Method> transactionalMethods;
    private final PlatformTransactionManager transactionManager;

    public TransactionHandler(Object target, PlatformTransactionManager transactionManager) {
        this.target = target;
        this.transactionalMethods = Arrays.stream(target.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Transactional.class))
                .collect(Collectors.toMap(Method::getName, method -> method));
        this.transactionManager = transactionManager;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (transactionalMethods.containsKey(method.getName())) {
            return invokeWithTransaction(target, method, args);
        }

        return method.invoke(target, args);
    }

    private Object invokeWithTransaction(Object target, Method method, Object[] args) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Object result = method.invoke(target, args);
            transactionManager.commit(status);
            return result;
        } catch (Exception e) {
            transactionManager.rollback(status);
            throw new DataAccessException(e);
        }
    }
}
