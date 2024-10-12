package com.interface21.jdbc.datasource;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;

public class DataAccessWrapper {

    private final DataSource dataSource;

    public DataAccessWrapper(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T apply(
            String sql,
            ThrowingFunction<PreparedStatement, T, Exception> function
    ) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            return function.apply(pstmt);
        } catch (Exception exception) {
            throw new DataAccessException(exception);
        } finally {
            closeUnActiveTransactionConnection(connection);
        }
    }

    private void closeUnActiveTransactionConnection(Connection connection) {
        try {
            if (!DataSourceUtils.isTransactionActive()) {
                connection.close();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}
