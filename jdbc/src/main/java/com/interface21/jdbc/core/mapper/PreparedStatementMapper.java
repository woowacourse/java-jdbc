package com.interface21.jdbc.core.mapper;

import com.interface21.jdbc.CannotReleaseJdbcResourceException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.annotation.ParametersAreNonnullByDefault;


@ParametersAreNonnullByDefault
public abstract class PreparedStatementMapper implements AutoCloseable {
    protected static final int DB_INDEX_OFFSET = 1;

    protected final PreparedStatement preparedStatement;
    protected final Object[] params;

    public PreparedStatementMapper(PreparedStatement preparedStatement, Object[] params) throws SQLException {
        this.preparedStatement = preparedStatement;
        this.params = params;
        checkConnection();
        setStatement();
    }

    protected abstract void setStatement() throws SQLException;

    private void checkConnection() throws SQLException {
        if (preparedStatement == null || preparedStatement.isClosed()) {
            throw new RuntimeException();
        }
    }

    public ResultSet executeQuery() {
        try {
            return preparedStatement.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int executeUpdate() {
        try {
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        try {
            preparedStatement.close();
        } catch (SQLException e) {
            throw new CannotReleaseJdbcResourceException(e);
        }
    }
}
