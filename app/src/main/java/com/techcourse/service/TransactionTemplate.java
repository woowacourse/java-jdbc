package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.ConnectionConsumerWrapper;
import com.interface21.jdbc.core.ServiceLogic;
import com.interface21.jdbc.datasource.DataSourceUtils;
import com.techcourse.config.DataSourceConfig;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import javax.sql.DataSource;

public class TransactionTemplate {

    private static final DataSource DATASOURCE = DataSourceConfig.getInstance();

    private TransactionTemplate() {
    }

    public static void executeWithTransaction(ServiceLogic logic) {
        Connection connection = DataSourceUtils.getConnection(DATASOURCE);
        try {
            validateConnection(connection);
            connection.setAutoCommit(false);
            logic.execute();
            connection.commit();
        } catch (SQLException e) {
            ConnectionConsumerWrapper.accept(connection, Connection::rollback);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, DATASOURCE);
        }
    }

    private static void validateConnection(Connection connection) throws SQLException {
        if (Objects.isNull(connection)) {
            throw new SQLException("Connection 존재하지 않습니다.");
        }
    }
}
