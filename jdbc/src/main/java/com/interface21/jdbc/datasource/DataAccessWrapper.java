package com.interface21.jdbc.datasource;

import com.interface21.dao.DataAccessException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DataAccessWrapper {

    public <T> T apply(
            Connection connection,
            String sql,
            ThrowingFunction<PreparedStatement, T, Exception> function
    ) {
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            return function.apply(pstmt);
        } catch (Exception exception) {
            throw new DataAccessException(exception);
        }
    }
}
