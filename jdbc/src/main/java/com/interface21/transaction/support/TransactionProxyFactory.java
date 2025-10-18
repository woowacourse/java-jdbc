package com.interface21.transaction.support;

import java.lang.reflect.Proxy;
import javax.sql.DataSource;

public class TransactionProxyFactory {

    private final DataSource dataSource;

    public TransactionProxyFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @SuppressWarnings("unchecked")
    public <T> T createProxy(T target, Class<T> interfaceType) {
        return (T) Proxy.newProxyInstance(
            interfaceType.getClassLoader(),
            new Class[]{interfaceType},
            new TransactionHandler(target, dataSource)
        );
    }
}
