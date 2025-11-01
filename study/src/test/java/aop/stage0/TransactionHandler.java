package aop.stage0;


import aop.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionHandler implements InvocationHandler {

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */

    private final PlatformTransactionManager platformTransactionManager;
    private final Object target;

    public TransactionHandler(final PlatformTransactionManager platformTransactionManager, final Object target) {
        this.platformTransactionManager = platformTransactionManager;
        this.target = target;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {

        final Transactional transactional = getMethodTransactional(method);

        if (transactional == null) {
            return invokeWithoutTransaction(method, args);
        }

        return invokeWithTransaction(method, args, transactional);
    }

    private Object invokeWithoutTransaction(Method method, Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    private Object invokeWithTransaction(Method method, Object[] args, Transactional transactional) throws Throwable {
        final TransactionStatus status = getTransactionStatus(transactional);

        try {
            final var ret = method.invoke(target, args);
            platformTransactionManager.commit(status);
            return ret;
        } catch (InvocationTargetException e) {
            platformTransactionManager.rollback(status);
            throw e.getTargetException();
        } catch (Exception e) {
            platformTransactionManager.rollback(status);
            throw e;
        }
    }

    private Transactional getMethodTransactional(Method method) throws NoSuchMethodException {
        return target.getClass()
                .getMethod(method.getName(), method.getParameterTypes())
                .getAnnotation(Transactional.class);
    }

    private TransactionStatus getTransactionStatus(Transactional transactional) {
        final DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(transactional.propagation().value());
        def.setIsolationLevel(transactional.isolation().value());
        def.setTimeout(transactional.timeout());
        def.setReadOnly(transactional.readOnly());

        return platformTransactionManager.getTransaction(def);
    }
}
