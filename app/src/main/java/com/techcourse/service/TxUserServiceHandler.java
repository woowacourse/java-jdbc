package com.techcourse.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import nextstep.jdbc.exception.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

public class TxUserServiceHandler implements InvocationHandler {

    private final PlatformTransactionManager transactionManager;
    private final UserService userService;

    public TxUserServiceHandler(PlatformTransactionManager transactionManager,
                                UserService userService) {
        this.transactionManager = transactionManager;
        this.userService = userService;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final TransactionStatus transaction = transactionManager.getTransaction(new DefaultTransactionDefinition());

        try {
            final Object returnObject = method.invoke(userService, args);
            transactionManager.commit(transaction);
            return returnObject;
        } catch (Exception exception) {
            transactionManager.rollback(transaction);
            throw new DataAccessException();
        }
    }
}
