package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;
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

    private static void initTransaction() {
        try {
            Connection connection = dataSource.getConnection();
            transaction.set(new Transaction(connection));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void commit() {
        getTransaction().commit();
    }

    public static void rollback() {
        getTransaction().rollback();
    }

    public static Transaction getTransaction() {
        return transaction.get();
    }
}
