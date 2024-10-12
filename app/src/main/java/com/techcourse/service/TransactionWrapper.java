package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.config.DataSourceConfig;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionWrapper {

    private TransactionWrapper() {
    }

    public static void executeWithTransaction(ServiceLogic logic) {
        DataSource dataSource = DataSourceConfig.getInstance();
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
            logic.execute(connection);
            connection.commit();
        } catch (SQLException e) {
            ConnectionFunctionWrapper.accept(connection, Connection::rollback);
            throw new DataAccessException(e);
        } finally {
            ConnectionFunctionWrapper.accept(connection, Connection::close);
        }
    }
}
