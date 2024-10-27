package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionTemplate {

    public static void executeTransaction(DataSource dataSource, TransactionCallback transactionCallback) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        TransactionSynchronizationManager.bindResource(dataSource, connection);
        try {
            connection.setAutoCommit(false);
            transactionCallback.execute();
            connection.commit();
        } catch (Exception e) {
            rollback(connection);
            throw new DataAccessException("트랜잭션 실행 중 예외 발생", e);
        } finally {
            TransactionSynchronizationManager.unbindResource(dataSource);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private static void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException ex) {
            throw new DataAccessException("데이터를 롤백하는 것에 실패했습니다", ex);
        }
    }
}
