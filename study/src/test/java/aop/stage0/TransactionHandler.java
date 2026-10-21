package aop.stage0;

import aop.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class TransactionHandler implements InvocationHandler {

    private final Object object;
    private final PlatformTransactionManager transactionManager;

    public TransactionHandler(Object object, PlatformTransactionManager transactionManager) {
        this.object = object;
        this.transactionManager = transactionManager;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        /* ===== 트랜잭션 영역 ===== */
        final var transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            /* ===== 트랜잭션 영역 ===== */
            /* ===== 애플리케이션 영역 ===== */
            method.invoke(this, proxy, args);
            /* ===== 애플리케이션 영역 ===== */

            /* ===== 트랜잭션 영역 ===== */
        } catch (RuntimeException e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
        transactionManager.commit(transactionStatus);
        /* ===== 트랜잭션 영역 ===== */
        return null;
    }
}
