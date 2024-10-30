package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionManager {

    private final DataSource dataSource;

    public TransactionManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }


    public void execute(Runnable runnable) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            setAutoCommit(connection, false);
            runnable.run();
            connection.commit();
        } catch (Exception e) {
            rollback(connection);
        } finally {
            setAutoCommit(connection, true);
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void setAutoCommit(Connection connection, boolean isAutoCommit) {
        try {
            connection.setAutoCommit(isAutoCommit);
        } catch (SQLException e) {
            throw new DataAccessException("autoCommit설정 중 에러가 발생했습니다" + e);
        }
    }
}
