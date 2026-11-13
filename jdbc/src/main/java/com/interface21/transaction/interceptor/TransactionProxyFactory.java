package com.interface21.transaction.interceptor;

import com.interface21.transaction.support.TransactionTemplate;
import java.lang.reflect.Proxy;
import java.util.Objects;
import javax.sql.DataSource;

public final class TransactionProxyFactory {

    private TransactionProxyFactory() {
    }

    public static <T> T create(final Class<T> serviceInterface, final T target, final DataSource dataSource) {
        Objects.requireNonNull(serviceInterface, "serviceInterface");
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(dataSource, "dataSource");
        if (!serviceInterface.isInterface() || !serviceInterface.isInstance(target)) {
            throw new IllegalArgumentException("The target must implement the supplied service interface");
        }

        final var handler = new TransactionInvocationHandler(target, new TransactionTemplate(dataSource));
        return serviceInterface.cast(Proxy.newProxyInstance(
                serviceInterface.getClassLoader(), new Class<?>[]{serviceInterface}, handler));
    }
}
