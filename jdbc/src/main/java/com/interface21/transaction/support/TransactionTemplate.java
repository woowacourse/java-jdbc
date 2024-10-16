package com.interface21.transaction.support;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.ConnectionConsumerWrapper;
import com.interface21.jdbc.datasource.DataSourceUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import javax.sql.DataSource;

public class TransactionTemplate {

    private final DataSource dataSource;

    public TransactionTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void executeWithTransaction(ServiceLogic logic) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            validateConnection(connection);
            connection.setAutoCommit(false);
            logic.execute();
            connection.commit();
        } catch (SQLException e) {
            ConnectionConsumerWrapper.accept(connection, Connection::rollback);
            throw new DataAccessException(e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private static void validateConnection(Connection connection) throws SQLException {
        if (Objects.isNull(connection)) {
            throw new SQLException("Connection 존재하지 않습니다.");
        }
    }
}
