package org.springframework.jdbc.support;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ConnectionHolder implements AutoCloseable {

    private static final int QUERY_PARAMETER_STEP = 1;

    private final Connection connection;
    private final boolean isTransactionActive;

    private ConnectionHolder(final Connection connection, final boolean isTransactionActive) {
        this.connection = connection;
        this.isTransactionActive = isTransactionActive;
    }

    public PreparedStatement createPrepareStatement(final String sql, final Object[] parameters) throws SQLException {
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);

        for (int index = 0; index < parameters.length; index++) {
            preparedStatement.setObject(index + QUERY_PARAMETER_STEP, parameters[index]);
        }

        return preparedStatement;
    }

    public static ConnectionHolder activeTransaction(final Connection connection) {
        return new ConnectionHolder(connection, true);
    }

    public static ConnectionHolder disableTransaction(final Connection connection) {
        return new ConnectionHolder(connection, false);
    }

    @Override
    public void close() throws Exception {
        if (!isTransactionActive) {
            connection.close();
        }
    }
}
