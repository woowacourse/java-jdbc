package aop.stage0;

import aop.Transactional;
import aop.service.UserService;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionHandler implements InvocationHandler {

    private PlatformTransactionManager transactionManager;
    private UserService target;

    public TransactionHandler(PlatformTransactionManager transactionManager, UserService userService) {
        this.transactionManager = transactionManager;
        this.target = userService;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (!isTransactional(method)) {
            return invokeWithoutTransaction(method, args);
        }

        final var status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            final var ret = method.invoke(target, args);
            transactionManager.commit(status);
            return ret;
        } catch (InvocationTargetException e) {
            transactionManager.rollback(status);
            throw e.getTargetException();
        }
    }

    private boolean isTransactional(Method method) throws NoSuchMethodException {
        Method targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
        return targetMethod.isAnnotationPresent(Transactional.class);
    }

    private Object invokeWithoutTransaction(Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }
}
