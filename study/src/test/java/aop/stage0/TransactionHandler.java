package aop.stage0;

import aop.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TransactionHandler implements InvocationHandler {

    private final Object target;
    private final PlatformTransactionManager transactionManager;

    public TransactionHandler(Object target, PlatformTransactionManager transactionManager) {
        this.target = target;
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (!isTransactional(method)) {                 // 메서드 선정
            return invokeTarget(method, args);
        }

        final var status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        final Object result;
        try {
            result = invokeTarget(method, args);
        } catch (Throwable e) {
            transactionManager.rollback(status);
            throw e;                                    // 원래 예외 그대로
        }
        transactionManager.commit(status);
        return result;                                  // 반환값 전달
    }

    private Object invokeTarget(final Method method, final Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);         // 타깃에, 인자 그대로
        } catch (InvocationTargetException e) {
            throw e.getTargetException();               // 리플렉션 포장 벗기기
        }
    }

    private boolean isTransactional(final Method method) throws NoSuchMethodException {
        final Method targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
        return targetMethod.isAnnotationPresent(Transactional.class);
    }
}
