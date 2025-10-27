package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DataSourceUtils {

    private static final Logger log = LoggerFactory.getLogger(DataSourceUtils.class);

    private DataSourceUtils() {
    }

    public static Connection getConnection(final DataSource dataSource) throws CannotGetJdbcConnectionException {
        final Connection con = (Connection) TransactionSynchronizationManager.getResource(dataSource);
        if (con != null) {
            return con;
        }
        try {
            return dataSource.getConnection();
        } catch (final SQLException e) {
            throw new CannotGetJdbcConnectionException("JDBC Connection을 가져오는데 실패했습니다.", e);
        }
    }

    public static void releaseConnection(
            final Connection con,
            final DataSource dataSource
    ) {
        if (TransactionSynchronizationManager.getResource(dataSource) == null) {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (final SQLException e) {
                log.error("JDBC Connection을 닫는 중 오류가 발생했습니다.", e);
            } catch (final Throwable e) {
                log.error("JDBC Connection을 닫는 중 예상치 못한 오류가 발생했습니다.", e);
            }
        }
    }
}
