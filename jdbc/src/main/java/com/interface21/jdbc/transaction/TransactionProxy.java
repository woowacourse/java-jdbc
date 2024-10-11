package com.interface21.jdbc.transaction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.sql.DataSource;

public class TransactionProxy implements InvocationHandler {

    private final Object target;
    private final DataSource dataSource;
    private final TransactionManager transactionManager;

    public TransactionProxy(Object target, DataSource dataSource, TransactionManager transactionManager) {
        this.target = target;
        this.dataSource = dataSource;
        this.transactionManager = transactionManager;
    }

    public static <T> T createProxy(T target, Class<T> targetType, DataSource dataSource, TransactionManager transactionManager) {
        Object transactionProxy = Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                new TransactionProxy(target, dataSource, transactionManager)
        );
        return targetType.cast(transactionProxy);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            transactionManager.begin(dataSource);
            Object result = method.invoke(target, args);
            transactionManager.commit(dataSource);

            return result;
        } catch (Throwable throwable) {
            transactionManager.rollback(dataSource);
            throw throwable.getCause();
        }
    }
}
