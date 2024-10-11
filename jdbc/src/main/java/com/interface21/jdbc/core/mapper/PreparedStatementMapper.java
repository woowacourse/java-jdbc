package com.interface21.jdbc.core.mapper;

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

    protected void setStatement() throws SQLException { //TODO: mapping strategy
        for (int index = 0; index < params.length; index++) {
            int databaseIndex = index + DB_INDEX_OFFSET;
            Object value = params[index];
            preparedStatement.setObject(databaseIndex, value);
        }
    }

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
    public void close() throws SQLException {
        preparedStatement.close();
    }
}
