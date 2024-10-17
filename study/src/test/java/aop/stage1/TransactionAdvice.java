package aop.stage1;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import aop.DataAccessException;

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
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Object result = invocation.proceed();
            transactionManager.commit(transactionStatus);
            return result;
        } catch (InvocationTargetException | DataAccessException e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException("트랜잭션 수행 중 예외가 발생해 롤백합니다.", e);
        }
    }
}
