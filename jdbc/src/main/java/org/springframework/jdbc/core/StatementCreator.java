package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StatementCreator {

    private static final String EMPTY_STRING = "";

    private StatementCreator() {
    }

    public static PreparedStatement createStatement(Connection connection, String sql, Object... arguments) {
        try {
            validateArgumentsCount(sql, arguments);

            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            setPreparedStatement(preparedStatement, arguments);
            return preparedStatement;
        } catch (SQLException e) {
            throw new IllegalStateException("creating statement - cannot connect database", e);
        }
    }

    private static void validateArgumentsCount(String sql, Object[] arguments) {
        int count = sql.split("\\?", -1).length - 1;
        if (arguments.length != count) {
            throw new IllegalArgumentException("parameterIndex does not correspond to a parameter marker in the SQL");
        }
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
