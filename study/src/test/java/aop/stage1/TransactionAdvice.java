package aop.stage1;

import aop.DataAccessException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

/**
 * 어드바이스(advice). 부가기능을 담고 있는 클래스
 */
public class TransactionAdvice implements MethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TransactionAdvice.class);

    private final PlatformTransactionManager transactionManager;

    public TransactionAdvice(final PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        final TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionAttribute());
        try {
            final Object result = invocation.proceed();
            transactionManager.commit(transactionStatus);
            return result;
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            log.warn("====> TransactionAdvice rollback");
            throw new DataAccessException(e);
        }
    }
}
