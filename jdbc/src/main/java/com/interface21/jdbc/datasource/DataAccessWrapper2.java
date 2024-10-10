package com.interface21.jdbc.datasource;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataAccessWrapper2 {

    public <T> T apply(
            Connection connection,
            String sql,
            ThrowingFunction<PreparedStatement, T, Exception> function
    ) {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            return function.apply(pstmt);
        } catch (Exception exception) {
            rollback(connection);
            throw new DataAccessException(exception);
        }
    }

    private void rollback(Connection connection) {
        try {
            connection.rollback();
            connection.setAutoCommit(true);
        } catch (SQLException sqlException) {
            throw new DataAccessException(sqlException);
        }
    }
}