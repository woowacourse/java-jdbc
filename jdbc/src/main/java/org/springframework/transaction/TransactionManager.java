package org.springframework.transaction;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(TransactionExecuter executer) {
        Connection conn = DataSourceUtils.getConnection(dataSource);

        try {
            conn.setAutoCommit(false);

            executer.execute(conn);

            conn.commit();
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
