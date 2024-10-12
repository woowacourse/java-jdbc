package com.interface21.jdbc.datasource;

import com.interface21.dao.DataAccessException;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
        }
    }
}
