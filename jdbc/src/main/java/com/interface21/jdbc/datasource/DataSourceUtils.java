package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class DataSourceUtils {

    private static final Logger log = LoggerFactory.getLogger(DataSourceUtils.class);

    private DataSourceUtils() {}

    /**
     * 현재 스레드에 바인딩된 연결이 있으면 그 연결을, 없으면 DataSource에서 새 연결을 돌려준다.
     * 새 연결은 바인딩하지 않는다. 바인딩은 트랜잭션을 시작하는 쪽의 몫이다.
     */
    public static Connection getConnection(final DataSource dataSource) throws CannotGetJdbcConnectionException {
        final var connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection != null) {
            return connection;
        }
        try {
            return dataSource.getConnection();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    /**
     * 바인딩된 연결은 트랜잭션이 끝날 때 그 주인이 닫으므로 그대로 두고, 그 밖의 연결만 닫는다.
     */
    public static void releaseConnection(final Connection connection, final DataSource dataSource) {
        if (connection == null || connection == TransactionSynchronizationManager.getResource(dataSource)) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ex) {
            log.warn("Failed to close JDBC Connection", ex);
        }
    }
}
