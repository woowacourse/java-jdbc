package com.techcourse.dao.statement;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import nextstep.jdbc.SingleResultSetExecutor;

public class SelectResultSet implements SingleResultSetExecutor<User> {

    @Override
    public User execute(final Connection connection, final String sql, final Object[] columns) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return getUser(columns, preparedStatement);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private User getUser(final Object[] columns, final PreparedStatement preparedStatement) throws SQLException {
        setColumns(columns, preparedStatement);
        return selectUser(preparedStatement);
    }

    private void setColumns(final Object[] columns, final PreparedStatement preparedStatement) throws SQLException {
        int parameterIndex = 1;
        for (Object column : columns) {
            preparedStatement.setObject(parameterIndex, column);
        }
    }

    private User selectUser(final PreparedStatement preparedStatement) {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            return binding(resultSet);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private User binding(final ResultSet resultSet) throws SQLException {
        if (resultSet.next()) {
            long id = resultSet.getLong("id");
            String account = resultSet.getString("account");
            String password = resultSet.getString("password");
            String email = resultSet.getString("email");
            return new User(id, account, password, email);
        }
        return null;
    }
}
