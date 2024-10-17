package aop.stage1;

import java.lang.reflect.Method;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * 어드바이스(advice). 부가기능을 담고 있는 클래스
 */
public class TransactionAdvice  implements MethodInterceptor {

    private final PlatformTransactionManager transactionManager;

    public TransactionAdvice(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            Object target = invocation.getThis();
            Method method = invocation.getMethod();
            Object[] arguments = invocation.getArguments();
            Object result = method.invoke(target, arguments);

            transactionManager.commit(transactionStatus);
            return result;
        } catch (Throwable e) {
            transactionManager.rollback(transactionStatus);
            throw e.getCause();
        }
    }
}
