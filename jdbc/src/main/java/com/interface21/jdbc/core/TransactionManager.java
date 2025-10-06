package com.interface21.jdbc.core;

import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import javax.sql.DataSource;

public class TransactionManager {

    private static final ThreadLocal<Transaction> transaction = new ThreadLocal<>();

    private static DataSource dataSource;

    public static void setDataSource(DataSource dataSource) {
        TransactionManager.dataSource = dataSource;
    }

    public static void start() {
        initTransaction();
        getTransaction().start();
    }

    public static void commit() {
        getTransaction().commit();
        releaseTransaction();
    }

    public static void rollback() {
        getTransaction().rollback();
        releaseTransaction();
    }

    public static Transaction getTransaction() {
        return transaction.get();
    }

    private static void initTransaction() {
        Connection newConnection = DataSourceUtils.getConnection(dataSource);
        transaction.set(new Transaction(newConnection));
    }

    private static void releaseTransaction() {
        DataSourceUtils.releaseConnection(getTransaction().getConnection(), dataSource);
        transaction.remove();
    }
}
