package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StatementCreator {

    private static final String EMPTY_STRING = "";

    private StatementCreator() {
    }

    public static PreparedStatement createStatement(Connection connection, String sql, Object... arguments) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        setPreparedStatement(preparedStatement, arguments);
        return preparedStatement;
    }

    private static void setPreparedStatement(PreparedStatement pstmt, Object... arguments) throws SQLException {
        for (int argumentIdx = 1; argumentIdx < arguments.length + 1; argumentIdx++) {
            Object argument = filterArgument(arguments[argumentIdx - 1]);
            pstmt.setObject(argumentIdx, argument);
        }
    }

    private static Object filterArgument(Object argument) {
        Object filtered = argument;
        if (argument instanceof String) {
            filtered = ((String) argument).replaceAll("[^a-zA-Z0-9-@.]", EMPTY_STRING);
        }
        return filtered;
    }
}
