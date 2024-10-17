package aop.stage0;

import aop.DataAccessException;
import aop.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionHandler implements InvocationHandler {

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    private final Object target;
    private final PlatformTransactionManager transactionManager;

    public TransactionHandler(Object target, PlatformTransactionManager transactionManager) {
        this.target = target;
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        Method targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
        if (!targetMethod.isAnnotationPresent(Transactional.class)) {
            return method.invoke(target, args);
        }

        final var transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Object result = method.invoke(target, args);
            transactionManager.commit(transactionStatus);
            return result;
        } catch (InvocationTargetException e) {
            transactionManager.rollback(transactionStatus);
            handleException(e.getCause());
            throw new RuntimeException("메서드 실행 중 오류: " + method.getName(), e);
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw new RuntimeException("메서드 실행 중 예외 발생: " + method.getName(), e);
        }
    }

    private void handleException(Throwable cause) {
        if (cause instanceof RuntimeException e) {
            throw e;
        }
        if (cause instanceof Exception e) {
            throw new DataAccessException(e);
        }
    }
}
