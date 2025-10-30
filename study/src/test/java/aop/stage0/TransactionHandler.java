package aop.stage0;

import aop.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
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
        Method targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());

        if (targetMethod.isAnnotationPresent(Transactional.class)) {
            var status = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());

            try {
                Object result = method.invoke(target, args);

                platformTransactionManager.commit(status);
                return result;
            } catch (InvocationTargetException e) {
                platformTransactionManager.rollback(status);
                throw e.getTargetException();
            } catch (Exception e) {
                platformTransactionManager.rollback(status);
                throw e;
            }
        }
        return method.invoke(target, args);
    }
}
