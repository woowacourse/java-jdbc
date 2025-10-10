package com.interface21.jdbc.core;

import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import javax.sql.DataSource;

public class TransactionManager {

    private final ThreadLocal<Transaction> transaction = new ThreadLocal<>();
    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
        TransactionManagerHolder.add(dataSource, this);
    }

    public void start() {
        initTransaction();
        getTransaction().start();
    }

    public void commit() {
        getTransaction().commit();
        releaseTransaction();
    }

    public void rollback() {
        getTransaction().rollback();
        releaseTransaction();
    }

    public Transaction getTransaction() {
        return transaction.get();
    }

    private void initTransaction() {
        Connection newConnection = DataSourceUtils.getConnection(dataSource);
        transaction.set(new Transaction(newConnection));
    }

    private void releaseTransaction() {
        DataSourceUtils.releaseConnection(getTransaction().getConnection(), dataSource);
        transaction.remove();
    }
}
