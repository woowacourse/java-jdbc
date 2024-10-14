package com.interface21.transaction.support;

import com.interface21.jdbc.core.JdbcTemplateException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;

public class TransactionManager {

    private final Connection connection;

    public TransactionManager(Connection connection) {
        this.connection = connection;
    }

    public void execute(Consumer<Connection> target) {
        try {
            connection.setAutoCommit(false);
            target.accept(connection);
            connection.commit();
        } catch (SQLException e) {
            throw new JdbcTemplateException("트랜잭션 처리 중 예외가 발생하여 롤백하였습니다.", e);
        }
    }
}
