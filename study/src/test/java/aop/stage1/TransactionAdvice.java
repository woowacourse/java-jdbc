package aop.stage1;

import aop.DataAccessException;
import java.lang.reflect.InvocationTargetException;
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

    private PlatformTransactionManager transactionManager;

    public TransactionAdvice(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            /* ===== 트랜잭션 영역 ===== */
            Method method = invocation.getMethod();
            Object[] arguments = invocation.getArguments();
            Object target = invocation.getThis();

            /* ===== 애플리케이션 영역 ===== */
            Object result = method.invoke(target, arguments);
            /* ===== 애플리케이션 영역 ===== */
            transactionManager.commit(transactionStatus);
            return result;
            /* ===== 트랜잭션 영역 ===== */
        } catch (InvocationTargetException e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
    }
}
