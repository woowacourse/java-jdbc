package aop.stage1;

import aop.DataAccessException;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.lang.reflect.Method;

/**
 * 어드바이스(advice). 부가기능을 담고 있는 클래스
 */
public class TransactionAdvice implements MethodInterceptor {

    private static final Logger log = LoggerFactory.getLogger(TransactionAdvice.class);
    private static final String TRANSACTION_FAIL_EXCEPTION = "Transaction을 실행하던 도중 실패했습니다.";

    private final PlatformTransactionManager transactionManager;

    public TransactionAdvice(PlatformTransactionManager platformTransactionManager) {
        this.transactionManager = platformTransactionManager;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        Method method = invocation.getMethod();
        Object[] args = invocation.getArguments();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();

        def.setName("transaction");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = transactionManager.getTransaction(def);

        try {
            Object result = method.invoke(invocation.getThis(), args);
            transactionManager.commit(status);
            return result;
        } catch (Exception e) {
            log.error(e.getMessage());
            transactionManager.rollback(status);
            throw new DataAccessException(TRANSACTION_FAIL_EXCEPTION, e);
        }
    }
}
