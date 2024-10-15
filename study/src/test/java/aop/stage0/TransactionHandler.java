package aop.stage0;

import aop.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionHandler implements InvocationHandler {

    private final PlatformTransactionManager transactionManager;
    private final Object target;

    public TransactionHandler(PlatformTransactionManager transactionManager, Object target) {
        this.transactionManager = transactionManager;
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (isNonTransactional(method)) {
            return method.invoke(target, args);
        }
        var status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Object result = method.invoke(target, args);
            transactionManager.commit(status);
            return result;
        } catch (Throwable throwable) {
            transactionManager.rollback(status);
            throw throwable.getCause();
        }
    }

    private boolean isNonTransactional(Method method) {
        return Arrays.stream(target.getClass().getMethods())
                .filter(targetMethod -> isSameMethodSignature(targetMethod, method))
                .findFirst()
                .map(targetMethod -> !targetMethod.isAnnotationPresent(Transactional.class))
                .orElseThrow();
    }

    private boolean isSameMethodSignature(Method targetMethod, Method method) {
        return targetMethod.getName().equals(method.getName())
                && Arrays.equals(targetMethod.getParameterTypes(), method.getParameterTypes());
    }
}

