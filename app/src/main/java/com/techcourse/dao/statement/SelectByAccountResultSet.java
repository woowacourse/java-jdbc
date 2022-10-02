package com.techcourse.dao.statement;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import nextstep.jdbc.SingleResultSetExecutor;

public class SelectByAccountResultSet implements SingleResultSetExecutor<User> {

    @Override
    public User execute(final Connection connection, final Object[] columns) {
        String sql = "select id, account, password, email from users where account = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            for (Object column : columns) {
                preparedStatement.setObject(parameterIndex, column);
            }

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    long id = resultSet.getLong("id");
                    String account = resultSet.getString("account");
                    String password = resultSet.getString("password");
                    String email = resultSet.getString("email");
                    return new User(id, account, password, email);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
