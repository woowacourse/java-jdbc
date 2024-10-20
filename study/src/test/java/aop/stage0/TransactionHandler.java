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
        if (hasNotTransactional(method)) {
            return method.invoke(target, args);
        }
        TransactionStatus status = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            Object invoke = method.invoke(target, args);

            platformTransactionManager.commit(status);
            return invoke;
        } catch (Exception e) {
            platformTransactionManager.rollback(status);
            throw new DataAccessException();
        }
    }

    private boolean hasNotTransactional(Method method) {
        return !target.getClass().isAnnotationPresent(Transactional.class) && hasNotMethodTransactional(method);
    }

    private boolean hasNotMethodTransactional(Method method) {
        return !Arrays.stream(target.getClass().getMethods())
                .filter(m -> m.getName().equals(method.getName()))
                .findFirst()
                .map(m -> m.isAnnotationPresent(Transactional.class))
                .orElseThrow(() -> new IllegalArgumentException("메소드가 존재하지 않습니다."));
    }
}
