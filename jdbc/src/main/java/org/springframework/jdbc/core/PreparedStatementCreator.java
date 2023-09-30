package org.springframework.jdbc.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PreparedStatementCreator {

    public PreparedStatement createPreparedStatement(
            final Connection connection,
            final String sql,
            final Object... args
    ) throws SQLException {
        final PreparedStatement preparedStatement = connection.prepareStatement(sql);
        bindArguments(preparedStatement, args);
        return preparedStatement;
    }

    private void bindArguments(final PreparedStatement preparedStatement, final Object[] args) throws SQLException {
        for (int index = 0; index < args.length; index++) {
            preparedStatement.setObject(index + 1, args[index]);
        }
    }
}
