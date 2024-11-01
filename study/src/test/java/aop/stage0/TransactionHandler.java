package aop.stage0;

import aop.DataAccessException;
import aop.Transactional;
import com.sun.jdi.InvocationException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionStatus;
import org.springframework.transaction.support.SimpleTransactionStatus;

public class TransactionHandler implements InvocationHandler {

    private final Object target;
    private final PlatformTransactionManager platformTransactionManager;

    public TransactionHandler(Object target, PlatformTransactionManager platformTransactionManager) {
        this.target = target;
        this.platformTransactionManager = platformTransactionManager;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Method realMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());

        if (!realMethod.isAnnotationPresent(Transactional.class)) {
            return realMethod.invoke(target, args);
        }

        TransactionStatus status = platformTransactionManager.getTransaction(TransactionDefinition.withDefaults());

        try {
            Object invoke = realMethod.invoke(target, args);
            platformTransactionManager.commit(status);
            return invoke;
        } catch (IllegalAccessException | InvocationTargetException e) {
            platformTransactionManager.rollback(status);
            throw new DataAccessException("exception occurred, rolling back..");
        }
    }
}
