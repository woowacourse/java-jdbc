package org.springframework.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import javax.annotation.Nullable;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Nullable
    public <T> T execute(TransactionExecuter<T> executer) {
        Connection conn = DataSourceUtils.getConnection(dataSource);

        try {
            conn.setAutoCommit(false);

            T execute = executer.execute(conn);

            conn.commit();
            return execute;
        } catch (Exception e) {
            rollback(conn);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ex) {
            throw new DataAccessException(ex);
        }
    }
}
