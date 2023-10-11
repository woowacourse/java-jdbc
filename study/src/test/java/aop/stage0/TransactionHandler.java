package aop.stage0;

import aop.DataAccessException;
import aop.Transactional;
import org.reflections.Reflections;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TransactionHandler implements InvocationHandler {

    //1. PlatformTransactionManager와 Target 객체를 주입받는다
    private final PlatformTransactionManager platformTransactionManager;
    private final Object target;

    public TransactionHandler(final PlatformTransactionManager platformTransactionManager, final Object target) {
        this.platformTransactionManager = platformTransactionManager;
        this.target = target;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        //2. 타겟 메서드 중에 매개변수로 들어온 메서드와 이름이 같은 고른다.
        //3. 고른 메서드에 트랜잭션 어노테이션이 있다면 트랜잭션 경계를 붙인다.
        Method tartgetMethod = target.getClass().getDeclaredMethod(method.getName(), method.getParameterTypes());
        if (tartgetMethod.isAnnotationPresent(Transactional.class)) {
            return invokeWithTransaction(tartgetMethod, args);
        }
        return method.invoke(target, args);
    }

    private Object invokeWithTransaction(final Method tartgetMethod, final Object[] args) {
        TransactionStatus status = platformTransactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            //4. 타겟 메소드에 타겟 객체와 메서드 실행에 필요한 매개변수를 넘겨줘, 메서드를 실행한다
            Object result = tartgetMethod.invoke(target, args);
            platformTransactionManager.commit(status);
            return result;
        } catch (Exception e) {
            platformTransactionManager.rollback(status);
            throw new DataAccessException(e);
        }
//        catch (InvocationTargetException e) {
//            throw new RuntimeException(e);
//        } catch (IllegalAccessException e) {
//            throw new RuntimeException(e);
//        }
    }
}
