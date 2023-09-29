package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class StatementCreator {

    public static PreparedStatement createStatement(Connection connection, String sql, Object... arguments) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(sql);

            int count = sql.split("\\?", -1).length - 1;
            if (arguments.length != count) {
                throw new IllegalArgumentException("맞지 않는 인자를 입력했습니다.");
            }

            setPreparedStatement(count, preparedStatement, arguments);
            return preparedStatement;
        } catch (SQLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static void setPreparedStatement(int count, PreparedStatement pstmt, Object... arguments) throws SQLException {
        for (int i = 1; i <= count; i++) {
            Object argument = arguments[i - 1];
            if (argument instanceof String) {
                pstmt.setString(i, ((String) argument).replaceAll("[^a-zA-Z0-9- ]", ""));
                continue;
            }
            if (argument instanceof Long) {
                pstmt.setLong(i, (long) argument);
                continue;
            }
            if (argument instanceof Integer) {
                pstmt.setInt(i, (int) argument);
                continue;
            }
            throw new IllegalArgumentException("지원하지 않는 타입입니다.");
        }
    }
}
