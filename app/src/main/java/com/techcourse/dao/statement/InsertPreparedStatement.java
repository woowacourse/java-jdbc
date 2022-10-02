package com.techcourse.dao.statement;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import nextstep.jdbc.PreparedStatementExecutor;

public class InsertPreparedStatement implements PreparedStatementExecutor {

    private final User user;

    public InsertPreparedStatement(final User user) {
        this.user = user;
    }

    @Override
    public void execute(final Connection connection, final String sql) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, user.getAccount());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getEmail());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
