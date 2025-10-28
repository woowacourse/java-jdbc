package aop.stage2;

import aop.Transactional;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@Aspect
@Component
public class TransactionAspect {

    private final PlatformTransactionManager platformTransactionManager;

    public TransactionAspect(PlatformTransactionManager platformTransactionManager) {
        this.platformTransactionManager = platformTransactionManager;
    }

    @Around("@annotation(transactional)")
    public Object transactional(ProceedingJoinPoint joinPoint, Transactional transactional) throws Throwable {
        final TransactionStatus status = getTransactionStatus(transactional);

        try {
            Object ret = joinPoint.proceed();
            platformTransactionManager.commit(status);
            return ret;
        } catch (RuntimeException e) {
            platformTransactionManager.rollback(status);
            throw e;
        }
    }

    private TransactionStatus getTransactionStatus(Transactional transactional) {
        final DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(transactional.propagation().value());
        def.setIsolationLevel(transactional.isolation().value());
        def.setTimeout(transactional.timeout());
        def.setReadOnly(transactional.readOnly());

        return platformTransactionManager.getTransaction(def);
    }
}
