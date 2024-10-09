package com.interface21.jdbc.transaction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.sql.DataSource;

public class TransactionProxy implements InvocationHandler {

    private final Object target;
    private final DataSource dataSource;

    public TransactionProxy(Object target, DataSource dataSource) {
        this.target = target;
        this.dataSource = dataSource;
    }

    public static <T> T createProxy(T target, Class<T> targetType, DataSource dataSource) {
        Object transactionProxy = Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                new TransactionProxy(target, dataSource)
        );
        return targetType.cast(transactionProxy);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        TransactionManager transactionManager = new TransactionManager(dataSource);
        try {
            transactionManager.begin();
            Object result = method.invoke(target, args);
            transactionManager.commit();

            return result;
        } catch (Throwable throwable) {
            transactionManager.rollback();
            throw throwable.getCause();
        }
    }
}
