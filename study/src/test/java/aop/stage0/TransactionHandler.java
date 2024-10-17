package aop.stage0;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import aop.DataAccessException;
import aop.Transactional;

public class TransactionHandler implements InvocationHandler {

    private final PlatformTransactionManager transactionManager;
    private final Object target;

    public TransactionHandler(PlatformTransactionManager transactionManager, Object target) {
        this.transactionManager = transactionManager;
        this.target = target;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        //method는 인터페이스의 메서드이다
        Method targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());
        if(targetMethod.isAnnotationPresent(Transactional.class)){
            return executeInTransaction(method, args);
        }
        return method.invoke(target, args);
    }

    private Object executeInTransaction(Method method, Object[] args) {
        TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try{
            Object result = method.invoke(target, args);
            transactionManager.commit(transactionStatus);
            return result;
        } catch (Exception e) {
            transactionManager.rollback(transactionStatus);
            throw new DataAccessException(e);
        }
    }
}
