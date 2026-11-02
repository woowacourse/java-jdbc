package aop.stage0;

import aop.Transactional;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
        final Method targetMethod = target.getClass()
                .getMethod(method.getName(), method.getParameterTypes());
        if (!targetMethod.isAnnotationPresent(Transactional.class)) {
            return method.invoke(target, args);
        }

        // todo 트랜잭션 경계 설정
        final TransactionStatus status = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Object ret;
            try {
                ret = method.invoke(target, args); // 타겟 객체의 메서드 실행
            } catch (InvocationTargetException e) {
                throw e.getTargetException();
            }
            platformTransactionManager.commit(status);
            return ret;
        } catch (Throwable e) {
            platformTransactionManager.rollback(status);
            throw e;
        }
        // todo 트랜잭션 경계 설정
    }
}
