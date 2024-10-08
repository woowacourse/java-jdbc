package com.interface21.jdbc.datasource;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.sql.DataSource;

public class DataAccessWrapper {

    private final DataSource dataSource;

    public DataAccessWrapper(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T apply(
            ThrowingBiFunction<Connection, PreparedStatement, T, Exception> function,
            String sql
    ) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement pstmt = connection.prepareStatement(sql);
        ) {
            return function.apply(connection, pstmt);
        } catch (Exception exception) {
            throw new DataAccessException(exception);
        }
    }
}
