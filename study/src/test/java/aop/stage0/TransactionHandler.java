package aop.stage0;

import aop.DataAccessException;
import aop.Transactional;
import aop.service.UserService;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionHandler implements InvocationHandler {

    private final PlatformTransactionManager transactionManager;
    private final UserService userService;

    public TransactionHandler(PlatformTransactionManager transactionManager, UserService userService) {
        this.transactionManager = transactionManager;
        this.userService = userService;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final Method targetMethod = userService.getClass().getMethod(method.getName(), method.getParameterTypes());

        if (!targetMethod.isAnnotationPresent(Transactional.class)) {
            return method.invoke(userService, args);   // 트랜잭션 없이 그냥 호출
        }

        final var transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            final var ret = method.invoke(userService, args);
            transactionManager.commit(transactionStatus);
            return ret;
        } catch (InvocationTargetException e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }

    }
}
