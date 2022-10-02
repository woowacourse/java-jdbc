package com.techcourse.dao.statement;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import nextstep.jdbc.PreparedStatementExecutor;

public class UpdatePreparedStatement implements PreparedStatementExecutor {

    private final User user;

    public UpdatePreparedStatement(final User user) {
        this.user = user;
    }

    @Override
    public void execute(final Connection connection) {
        String sql = "update users set account = ?, password = ?, email = ? where id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, user.getAccount());
            preparedStatement.setString(2, user.getPassword());
            preparedStatement.setString(3, user.getEmail());
            preparedStatement.setLong(4, user.getId());
            preparedStatement.executeUpdate();
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
