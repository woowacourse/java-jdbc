package com.interface21.jdbc.datasource;

import com.interface21.jdbc.CannotGetJdbcConnectionException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

/**
 * JDBC 트랜잭션 컨텍스트 내에서 Connection을 안전하게 열고 닫을 수 있게 도와주는 클래스.
 * 같은 트랜잭션 내에서는 같은 Connection을 공유하게 해준다.
 *
 * 인스턴스화할 필요도, 상속할 필요도 없으므로 abstract + private 생성자를 사용한다.
 */
public abstract class DataSourceUtils {

    private DataSourceUtils() {}

    /**
     * 현재 트랜잭션 컨텍스트에서 Connection을 가져온다.
     * 1. 현재 스레드에 바인딩된 커넥션이 있으면 그걸 반환한다.
     * 2. 없으면 DataSource에서 새 커넥션을 열고, 트랜잭션 매니저가 관리할 수 있도록 바인딩한다.
     *
     * @param dataSource 커넥션을 가져올 데이터소스
     * @return 현재 트랜잭션 컨텍스트에 바인딩된 커넥션 또는 새로 생성된 커넥션
     */
    public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
        Connection connection = TransactionSynchronizationManager.getResource(dataSource);
        if (connection != null) {
            return connection;
        }

        try {
            connection = dataSource.getConnection();
            TransactionSynchronizationManager.bindResource(dataSource, connection);
            return connection;
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to obtain JDBC Connection", ex);
        }
    }

    /**
     * 사용이 끝난 트랜잭션을 해제한다.
     * 아직 끝나지 않은 트랜잭션에 포함된 커넥션은 해제하지 않는다.
     *
     * @param connection 해제할 커넥션
     */
    public static void releaseConnection(Connection connection, DataSource dataSource) {
        if (connection == null) {
            return;
        }

        Connection transactionalConnection = TransactionSynchronizationManager.getResource(dataSource);
        if (transactionalConnection != null && transactionalConnection == connection) {
            return;
        }

        // 트랜잭션 외부의 커넥션은 직접 닫는다.
        try {
            connection.close();
        } catch (SQLException ex) {
            throw new CannotGetJdbcConnectionException("Failed to close JDBC Connection", ex);
        }
    }
}
