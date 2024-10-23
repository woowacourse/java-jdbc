package aop.stage0;

import aop.DataAccessException;
import aop.Transactional;
import aop.service.UserService;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TransactionHandler implements InvocationHandler {

    private final PlatformTransactionManager transactionManager;
    private final UserService target;

    public TransactionHandler(final PlatformTransactionManager transactionManager, final UserService target) {
        this.transactionManager = transactionManager;
        this.target = target;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        // 인자로 들어온 method는 UserService 인터페이스의 메서드이기 때문에 @Transactional 어노테이션이 적용되지 않음.
        // target에 @Transactional 어노테이션이 부착되어 있는지 확인하기 위함
        Method targetMethod = target.getClass().getMethod(method.getName(), method.getParameterTypes());

        if (targetMethod.isAnnotationPresent(Transactional.class)) {
            TransactionStatus transactionStatus = transactionManager.getTransaction(new DefaultTransactionDefinition());
            try {
                Object result = method.invoke(target, args);
                transactionManager.commit(transactionStatus);
                return result;
            } catch (Exception e) {     // method.invoke()가 checked exception을 던지기 때문에 Exception을 처리하도록 변경
                transactionManager.rollback(transactionStatus);
                throw new DataAccessException(e);
            }
        }
        return method.invoke(target, args);
    }
}
