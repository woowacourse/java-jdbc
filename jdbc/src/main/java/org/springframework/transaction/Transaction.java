package org.springframework.transaction;

import java.sql.Connection;

public class Transaction {

    private final TransactionTemplate transactionTemplate;

    public Transaction(Connection connection) {
        this.transactionTemplate = new TransactionTemplate(connection);
    }

    public void start() {
        transactionTemplate.execute(connection -> connection.setAutoCommit(false));
    }

    public void commit() {
        transactionTemplate.execute(Connection::commit);
        close();
    }

    private void close() {
        transactionTemplate.execute(Connection::close);
    }

    public void rollback() {
        transactionTemplate.execute(Connection::rollback);
        close();
    }

    public Connection getConnection() {
        return transactionTemplate.getConnection();
    }

}
