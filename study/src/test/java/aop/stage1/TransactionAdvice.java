package aop.stage1;

import aop.DataAccessException;
import aop.Transactional;
import java.lang.reflect.Method;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

/**
 * 어드바이스(advice). 부가기능을 담고 있는 클래스
 */
public class TransactionAdvice implements MethodInterceptor {

    private final PlatformTransactionManager platformTransactionManager;

    public TransactionAdvice(PlatformTransactionManager platformTransactionManager) {
        this.platformTransactionManager = platformTransactionManager;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        Method targetMethod = invocation.getMethod();

        if (!targetMethod.isAnnotationPresent(Transactional.class)) {
            return invocation.proceed();
        }

        TransactionStatus status = platformTransactionManager.getTransaction(TransactionDefinition.withDefaults());
        try {
            Object result = invocation.proceed();
            platformTransactionManager.commit(status);
            return result;
        } catch (Exception e) {
            platformTransactionManager.rollback(status);
            throw new DataAccessException(e);
        }
    }
}
