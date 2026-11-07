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

    /** 트랜잭션에 바인딩된 연결이 있으면 그 연결을, 없으면 스레드에 보관하지 않는 새 연결을 반환한다. */
    public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
        final Connection connection = TransactionSynchronizationManager.getResource(dataSource);
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
     * 트랜잭션에 바인딩된 연결은 트랜잭션을 시작한 쪽이 닫도록 남겨두고, 그 외의 연결만 닫는다.
     * 닫기 실패는 이미 끝난 작업의 결과나 진행 중인 예외를 바꾸지 않도록 로그로 남긴다.
     */
    public static void releaseConnection(Connection connection, DataSource dataSource) {
        if (connection == TransactionSynchronizationManager.getResource(dataSource)) {
            return;
        }
        try {
            connection.close();
        } catch (SQLException ex) {
            log.warn("Failed to close JDBC Connection", ex);
        }
    }
}
