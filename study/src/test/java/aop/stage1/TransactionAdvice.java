package aop.stage1;

import aop.DataAccessException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * 어드바이스(advice). 부가기능을 담고 있는 클래스
 */
public class TransactionAdvice implements MethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TransactionAdvice.class);

    private final PlatformTransactionManager platformTransactionManager;

    public TransactionAdvice(PlatformTransactionManager platformTransactionManager) {
        this.platformTransactionManager = platformTransactionManager;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        String methodName = invocation.getMethod().getName();
        TransactionStatus transactionStatus = platformTransactionManager.getTransaction(
                new DefaultTransactionDefinition());
        try {
            Object ret = invocation.proceed();
            log.info("proceed: {}", methodName);
            platformTransactionManager.commit(transactionStatus);
            log.info("transaction commit: {}", methodName);
            return ret;
        } catch (Exception e) {
            platformTransactionManager.rollback(transactionStatus);
            log.info("transaction roll back: {}", methodName);
            throw new DataAccessException(e);
        }
    }
}
