package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StatementCreator {

    private StatementCreator() {
    }

    public static PreparedStatement createStatement(Connection connection, String sql, Object... arguments) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        setPreparedStatement(preparedStatement, arguments);
        return preparedStatement;
    }

    private static void setPreparedStatement(PreparedStatement pstmt, Object... arguments) throws SQLException {
        for (int argumentIdx = 1; argumentIdx < arguments.length + 1; argumentIdx++) {
            pstmt.setObject(argumentIdx, arguments[argumentIdx - 1]);
        }
    }
}
