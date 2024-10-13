package aop.stage0;

import aop.DataAccessException;
import aop.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
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

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        // method는 인터페이스의 메서드이므로 target의 메서드를 찾아야 한다.
        Method targetMethod = target.getClass()
                .getMethod(method.getName(), method.getParameterTypes());
        if (!targetMethod.isAnnotationPresent(Transactional.class)) {
            return method.invoke(target, args);
        }

        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        Object result = null;
        try {
            result = method.invoke(target, args);
        } catch (Throwable e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
        transactionManager.commit(transactionStatus);
        return result;
    }
}
