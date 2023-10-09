package org.springframework.jdbc.core;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T transaction(TransactionExecutor<T> transactionExecutor) throws SQLException {
        Connection conn = DataSourceUtils.getConnection(dataSource);
        conn.setAutoCommit(false);
        try {
            T result = transactionExecutor.execute();
            conn.commit();
            conn.setAutoCommit(true);
            return result;
        } catch (SQLException | DataAccessException e) {
            e.printStackTrace();
            conn.rollback();
            conn.setAutoCommit(true);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }
}
