package aop.stage2;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

@Aspect
@Component
public class TransactionAspect {

    private final PlatformTransactionManager platformTransactionManager;

    public TransactionAspect(final PlatformTransactionManager platformTransactionManager) {
        this.platformTransactionManager = platformTransactionManager;
    }

    @Around("@annotation(aop.Transactional)")
    public Object aroundTransactionalMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        TransactionStatus status = platformTransactionManager.getTransaction(null);

        try {
            Object result = joinPoint.proceed();
            platformTransactionManager.commit(status);

            return result;
        } catch (Exception e) {
            platformTransactionManager.rollback(status);

            throw e;
        }
    }
}
