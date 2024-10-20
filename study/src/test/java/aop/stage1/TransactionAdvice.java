package aop.stage1;

import aop.DataAccessException;
import java.lang.reflect.InvocationTargetException;
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

    private final PlatformTransactionManager transactionManager;

    public TransactionAdvice(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        log.info("-- 트랜잭션 시작 : " + invocation.getMethod().getName());
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Object result = invocation.proceed();
            transactionManager.commit(transactionStatus);
            log.error("성공! commit");
            return result;
        } catch (InvocationTargetException | DataAccessException e) {
            transactionManager.rollback(transactionStatus);
            log.error("실패! rollback");
            throw new DataAccessException(e);
        } finally {
            log.info("-- 트랜잭션 종료 : " + invocation.getMethod().getName());
        }
    }
}
