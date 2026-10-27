package aop.stage1;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * 어드바이스(advice). 부가기능을 담고 있는 클래스
 *
 * 부가 기능 실행: 트랜잭션 시작·커밋·롤백
 */
public class TransactionAdvice implements MethodInterceptor {

    private final PlatformTransactionManager transactionManager;

    public TransactionAdvice(final PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final var status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        final Object result;
        try {
            result = invocation.proceed();
        } catch (Throwable e) {
            transactionManager.rollback(status);
            throw e;
        }

        transactionManager.commit(status);
        return result;
    }
}
