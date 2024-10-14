package aop.stage1;

import aop.DataAccessException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * 어드바이스(advice). 부가기능을 담고 있는 클래스
 */
public class TransactionAdvice implements MethodInterceptor {

    private final PlatformTransactionManager transactionManager;

    public TransactionAdvice(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        /* ===== 트랜잭션 영역 ===== */
        final var transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        Object result;
        try {
            /* ===== 트랜잭션 영역 ===== */

            /* ===== 애플리케이션 영역 ===== */
            result = invocation.proceed();
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
