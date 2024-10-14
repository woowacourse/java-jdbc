package com.interface21.jdbc.core.mapper;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.jdbc.CannotReleaseJdbcResourceException;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public abstract class PreparedStatementMapper implements AutoCloseable {
    protected static final int DB_INDEX_OFFSET = 1;

    protected final PreparedStatement preparedStatement;
    protected final Object[] params;
    protected JDBCType[] jdbcType;

    public PreparedStatementMapper(PreparedStatement preparedStatement, Object[] params) throws SQLException {
        this.preparedStatement = preparedStatement;
        this.params = params;
        checkConnection();
        setStatement();
    }

    public PreparedStatementMapper(PreparedStatement preparedStatement, Object[] params, JDBCType[] jdbcType) {
        this.preparedStatement = preparedStatement;
        this.params = params;
        this.jdbcType = jdbcType;
    }

    protected abstract void setStatement() throws SQLException;

    private void checkConnection() throws SQLException {
        if (preparedStatement == null || preparedStatement.isClosed()) {
            throw new CannotGetJdbcConnectionException("prepared statement is not available.");
        }
    }

    public ResultSet executeQuery() throws SQLException {
        return preparedStatement.executeQuery();
    }

    public int executeUpdate() throws SQLException {
        return preparedStatement.executeUpdate();
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
