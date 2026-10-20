package aop.stage0;

import aop.DataAccessException;
import aop.stage1.UserService;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.xml.crypto.Data;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionHandler implements InvocationHandler {

    private final PlatformTransactionManager transactionManager;
    private final Object target;

    public TransactionHandler(PlatformTransactionManager transactionManager, Object target) {
        this.transactionManager = transactionManager;
        this.target = target;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        Object result;
        final var status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            result = method.invoke(target, args);
        } catch (InvocationTargetException e) {
            transactionManager.rollback(status);
            throw e.getCause();
        }

        transactionManager.commit(status);
        return result;
    }
}
