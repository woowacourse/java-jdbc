package org.springframework.transaction.support;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.datasource.DataSourceUtils;

public class TxTemplate {

    private final DataSource dataSource;

    public TxTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void execute(Runnable callBack) {
        try (Connection conn = DataSourceUtils.getConnection(dataSource)) {
            try {
                conn.setAutoCommit(false);
                callBack.run();
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new DataAccessException(e);
            } finally {
                DataSourceUtils.releaseConnection(conn, dataSource);
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
