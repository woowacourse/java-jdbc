package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PreparedStatementUtil {

    public static PreparedStatement getPreparedStatement(final Connection connection, final String sql, final Object... conditions) {
        try {
            final PreparedStatement preparedStatement = connection.prepareStatement(sql);
            setConditions(preparedStatement, conditions);
            return preparedStatement;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static void setConditions(PreparedStatement preparedStatement, Object[] conditions) throws SQLException {
        for (int i = 1; i <= conditions.length; i++) {
            preparedStatement.setObject(i, conditions[i - 1]);
        }
    }

    public static ResultSet getResultSet(final PreparedStatement preparedStatement) {
        try {
            return preparedStatement.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
