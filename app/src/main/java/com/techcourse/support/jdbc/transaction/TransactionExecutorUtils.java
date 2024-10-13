package com.techcourse.support.jdbc.transaction;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionExecutorUtils {

    private static final Logger log = LoggerFactory.getLogger(TransactionExecutorUtils.class);

    private TransactionExecutorUtils() {
    }

    public static void executeInTransaction(Runnable callback) {
        executeInTransaction(() -> {
            callback.run();
            return null;
        });
    }

    public static <T> T executeInTransaction(Supplier<T> callback) {
        log.debug("-- 트랜잭션 시작");
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            conn.setAutoCommit(false);
            T result = callback.get();
            conn.commit();
            return result;
        } catch (SQLException | DataAccessException e) {
            rollback(conn);
            throw new DataAccessException(e);
        } finally {
            releaseConnection(conn, dataSource);
            log.debug("-- 트랜잭션 종료");
        }
    }

    private static void rollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException ignored) {
            log.error("rollback 실패", ignored);
        }
    }

    private static void releaseConnection(Connection conn, DataSource dataSource) {
        DataSourceUtils.releaseConnection(conn, dataSource);
        try {
            conn.setAutoCommit(true);
        } catch (SQLException ignored) {
        }
    }
}
