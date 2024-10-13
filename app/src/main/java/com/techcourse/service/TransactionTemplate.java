package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.ConnectionConsumerWrapper;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public class TransactionTemplate {

    private TransactionTemplate() {
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
            ConnectionConsumerWrapper.accept(connection, Connection::rollback);
            throw new DataAccessException(e);
        } finally {
            ConnectionConsumerWrapper.accept(connection, Connection::close);
        }
    }
}
