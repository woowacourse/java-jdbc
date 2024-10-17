package aop.stage1;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import aop.DataAccessException;

/**
 * 어드바이스(advice). 부가기능을 담고 있는 클래스
 * MethodInterceptor는 Spring AOP에서 메서드 호출을 가로채기 위해 사용하는 인터페이스이다.
 * 해당 인터페이스의 invoke 메서드를 구현하여 AOP의 부가기능(어드바이스)를 정의한다.
 */
public class TransactionAdvice implements MethodInterceptor {

    /**
     * PlatformTransactionManager: Spring의 트랜잭션 관리 인터페이스로, 데이터베이스 트랜잭션을 관리한다.
     */
    private final PlatformTransactionManager transactionManager;

    public TransactionAdvice(PlatformTransactionManager platformTransactionManager) {
        this.transactionManager = platformTransactionManager;
    }

    /**
     * 실제 메서드를 호출하기 전에 트랜잭션을 시작하고, 메서드 실행 결과에 따라 트랜잭션을 처리한다.
     */
    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            Object result = invocation.proceed(); // 실제 메서드 호출
            transactionManager.commit(transactionStatus);
            return result;
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
    }
}
