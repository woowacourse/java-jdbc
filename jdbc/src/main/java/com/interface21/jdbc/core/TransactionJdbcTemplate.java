package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionJdbcTemplate extends JdbcTemplate {

    private static final Logger log = LoggerFactory.getLogger(TransactionJdbcTemplate.class);

    private Connection connection = null;

    public TransactionJdbcTemplate(final DataSource dataSource) {
        super(dataSource);
    }

    public void executeInTransaction(ConnectionCallback connectionCallback) {
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            connectionCallback.execute(connection);
            connection.commit();
        } catch (Exception e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    throw new DataAccessException(ex.getMessage(), ex);
                }
            }
            throw new DataAccessException(e.getMessage(), e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new DataAccessException(e.getMessage(), e);
            } finally {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    throw new DataAccessException(ex.getMessage(), ex);
                } finally {
                    connection = null;
                }
            }
        }
    }

    protected <T> T executeQuery(String sql, PreparedStatementCallBack<T> callBack) {
        if (connection == null) {
            return super.executeQuery(sql, callBack);
        }
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            return callBack.execute(pstmt);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            throw new DataAccessException(e.getMessage(), e);
        }
    }
}
