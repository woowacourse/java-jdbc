package com.interface21.transaction.support;

import com.interface21.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

public final class TransactionalProxy implements InvocationHandler {

    private final Object target;
    private final TransactionTemplate transactionTemplate;

    private TransactionalProxy(final Object target, final DataSource dataSource) {
        this.target = Objects.requireNonNull(target);
        this.transactionTemplate = new TransactionTemplate(dataSource);
    }

    public static <T> T create(final Class<T> serviceType, final T target, final DataSource dataSource) {
        return serviceType.cast(Proxy.newProxyInstance(serviceType.getClassLoader(),
                new Class<?>[]{serviceType}, new TransactionalProxy(target, dataSource)));
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final Method implementation = target.getClass().getMethod(method.getName(), method.getParameterTypes());
        if (!method.isAnnotationPresent(Transactional.class)
                && !implementation.isAnnotationPresent(Transactional.class)) {
            return invokeTarget(implementation, args);
        }
        return transactionTemplate.execute(() -> invokeTarget(implementation, args));
    }

    private Object invokeTarget(final Method method, final Object[] args) throws Throwable {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException exception) {
            throw exception.getCause();
        }
    }
}
