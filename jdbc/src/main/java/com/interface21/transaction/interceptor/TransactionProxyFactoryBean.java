package com.interface21.transaction.interceptor;

import com.interface21.transaction.support.TransactionHandler;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;

public class TransactionProxyFactoryBean {

    private Object target;
    private DataSource dataSource;

    public void setTarget(Object target) {
        this.target = target;
    }

    public void setDataSource(DataSource dataSource) {
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
