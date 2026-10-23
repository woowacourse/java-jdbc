package aop.stage0;

import aop.service.UserService;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import org.springframework.transaction.PlatformTransactionManager;

public class TransactionHandler implements InvocationHandler {

    private final PlatformTransactionManager platformTransactionManager;
    private final UserService userService;

    public TransactionHandler(PlatformTransactionManager platformTransactionManager, UserService userService) {
        this.platformTransactionManager = platformTransactionManager;
        this.userService = userService;
    }

    /**
     * @Transactional 어노테이션이 존재하는 메서드만 트랜잭션 기능을 적용하도록 만들어보자.
     */
    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        return method.invoke(userService, args);
    }
}
