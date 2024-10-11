package com.interface21.jdbc;

import com.interface21.context.stereotype.Component;
import com.interface21.context.stereotype.Inject;
import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import javax.sql.DataSource;

@Component
public class TransactionManager {

    @Inject
    private DataSource dataSource;

    private TransactionManager() {}

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(Consumer<Connection> consumer) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            consumer.accept(connection);
            connection.commit();
        } catch (SQLException e) {
            rollback(connection);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
