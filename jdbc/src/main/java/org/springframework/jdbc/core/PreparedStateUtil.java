package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStateUtil {

    private PreparedStateUtil() {
    }

    public static PreparedStatement makeStatement(final Connection connection, final String sql, final Object... parameters) throws SQLException {
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        bindingParams(preparedStatement, parameters);
        return preparedStatement;
    }

    private static void bindingParams(final PreparedStatement preparedStatement, final Object... parameters) throws SQLException {
        for (int i = 0; i < parameters.length; i++) {
            preparedStatement.setObject(i + 1, parameters[i]);
        }
    }
}
