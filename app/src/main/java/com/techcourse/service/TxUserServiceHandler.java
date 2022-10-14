package com.techcourse.service;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.sql.DataSource;
import nextstep.jdbc.exception.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
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

    public static UserService from (DataSource dataSource, UserService targetUserService) {
        final PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);

        return (UserService) Proxy.newProxyInstance(
                ClassLoader.getSystemClassLoader(),
                new Class[]{UserService.class},
                new TxUserServiceHandler(transactionManager, targetUserService));
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
