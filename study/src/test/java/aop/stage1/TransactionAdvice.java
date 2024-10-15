package aop.stage1;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.lang.reflect.InvocationTargetException;

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
        TransactionStatus transactionStatus = platformTransactionManager.getTransaction(TransactionDefinition.withDefaults());

        try {
            Object object = invocation.proceed();
            platformTransactionManager.commit(transactionStatus);
            return object;
            //메서드에서 발생한 오류
        } catch (InvocationTargetException e) {
            platformTransactionManager.rollback(transactionStatus);
            throw e.getCause();
            //메서드에서 직접 던지는 오류는 여기 해당
        } catch (Throwable throwable) {
            platformTransactionManager.rollback(transactionStatus);
            throw throwable;
        }
    }
}
