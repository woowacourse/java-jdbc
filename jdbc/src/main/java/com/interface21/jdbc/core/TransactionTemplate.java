package com.interface21.jdbc.core;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import com.interface21.dao.DataAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionTemplate {

    private static final Logger log = LoggerFactory.getLogger(TransactionTemplate.class);

    public <T> T execute(Connection con, Supplier<T> supplier) {
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

    public void executeWithoutResult(Connection con, Runnable runnable) {
        execute(con, () -> {
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
                con.close();
            } catch (Exception e) {
                log.info("release error", e);
            }
        }
    }
}
