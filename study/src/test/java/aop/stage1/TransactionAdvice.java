package aop.stage1;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * 어드바이스(advice). 부가기능을 담고 있는 클래스
 */
public class TransactionAdvice  implements MethodInterceptor {

    private PlatformTransactionManager transactionManager;

    public TransactionAdvice(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final var status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        final Object ret;
        try {
            ret = invocation.proceed();
        } catch (Throwable e) {
            completeAfterThrowing(status, e);
            throw e;
        }
        transactionManager.commit(status);
        return ret;
    }

    private void completeAfterThrowing(TransactionStatus status, Throwable e) {
        if (isRollbackTarget(e)) {
            transactionManager.rollback(status);
            return;
        }
        transactionManager.commit(status);
    }

    private boolean isRollbackTarget(Throwable e) {
        return e instanceof RuntimeException || e instanceof Error;
    }
}
