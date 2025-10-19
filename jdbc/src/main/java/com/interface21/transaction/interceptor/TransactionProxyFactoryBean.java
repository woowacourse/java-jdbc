package com.interface21.transaction.interceptor;

import com.interface21.transaction.support.TransactionHandler;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;

public class TransactionProxyFactoryBean {

    private final Object target;
    private final DataSource dataSource;

    public TransactionProxyFactoryBean(Object target, DataSource dataSource) {
        this.target = target;
        this.dataSource = dataSource;
    }

    public Object getObject() {
        TransactionHandler handler = new TransactionHandler(dataSource, target);

        return Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                handler
        );
    }

}
