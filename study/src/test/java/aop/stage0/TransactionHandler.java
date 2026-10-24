package aop.stage0;

import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class TransactionHandler implements InvocationHandler {

    private final PlatformTransactionManager transactionManager;
    private final Object target;

    public TransactionHandler(final PlatformTransactionManager transactionManager, final Object target) {
        this.transactionManager = transactionManager;
        this.target = target;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        // TODO 2: TxUserService처럼 getTransaction → try/commit → catch/rollback 감싸기
        // TODO 3: @Transactional 붙은 메서드에만 적용하기
        return method.invoke(target, args);
    }
}
