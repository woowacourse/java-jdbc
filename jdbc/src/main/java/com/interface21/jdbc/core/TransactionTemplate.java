package com.interface21.jdbc.core;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionTemplate {

    private static final Logger log = LoggerFactory.getLogger(TransactionTemplate.class);

    private final DataSource datasource;

    public TransactionTemplate(DataSource datasource) {
        this.datasource = datasource;
    }

    public <T> T execute(Supplier<T> supplier) {
        Connection con = DataSourceUtils.getConnection(datasource);

        try {
            con.setAutoCommit(false);
            T result = supplier.get();
            con.commit();

            return result;
        } catch (Exception e) {
            rollback(con);
            throw new DataAccessException(e);
        } finally {
            release(con);
        }
    }

    public void executeWithoutResult(Runnable runnable) {
        execute(() -> {
            runnable.run();
            return null;
        });
    }

    private void rollback(Connection con) {
        try {
            con.rollback();
        } catch (SQLException e) {
            log.error("rollback error", e);
        }
    }

    private void release(Connection con) {
        if (con != null) {
            try {
                con.setAutoCommit(true);
                DataSourceUtils.releaseConnection(con, datasource);
                TransactionSynchronizationManager.unbindResource(datasource);
            } catch (Exception e) {
                log.info("release error", e);
            }
        }
    }
}
