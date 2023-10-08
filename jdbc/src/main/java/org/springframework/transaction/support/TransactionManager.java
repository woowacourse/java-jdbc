package org.springframework.transaction.support;

import org.springframework.jdbc.datasource.ConnectionUtils;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(final LogicExecutor logicExecutor) {
        final Connection conn = ConnectionUtils.getConnection(dataSource);
        try {
            conn.setAutoCommit(false);
            logicExecutor.run(conn);
            conn.commit();
        } catch (Exception e) {
            rollback(conn);
            throw new IllegalStateException(e);
        } finally {
            ConnectionUtils.releaseConnection(conn);
        }
    }

private void rollback(final Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
