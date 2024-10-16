package aop.stage0;

import aop.DataAccessException;
import aop.Transactional;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class TransactionHandler implements InvocationHandler {

    private final Map<String, Method> implMethods = new HashMap<>();
    private final PlatformTransactionManager transactionManager;
    private final Object service;

    public TransactionHandler(Object service, PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        this.service = service;
        initMethods();
    }

    private void initMethods() {
        for (Method method : service.getClass().getMethods()) {
            implMethods.put(method.getName(), method);
        }
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        Method implMethod = implMethods.get(method.getName());
        if (!implMethod.isAnnotationPresent(Transactional.class)) {
            return implMethod.invoke(service, args);
        }

        /* ===== 트랜잭션 영역 ===== */
        final var transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        Object result;
        try {
            /* ===== 트랜잭션 영역 ===== */

            /* ===== 애플리케이션 영역 ===== */
            result = implMethod.invoke(service, args);
            /* ===== 애플리케이션 영역 ===== */

            /* ===== 트랜잭션 영역 ===== */
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
        transactionManager.commit(transactionStatus);
        /* ===== 트랜잭션 영역 ===== */
        return result;
    }
}
