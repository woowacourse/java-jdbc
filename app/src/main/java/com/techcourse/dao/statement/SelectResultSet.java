package com.techcourse.dao.statement;

import com.techcourse.domain.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import nextstep.jdbc.AbstractResultSetExecutor;

public class SelectResultSet extends AbstractResultSetExecutor<User> {

    @Override
    protected User executeQuery(final PreparedStatement preparedStatement, final Object[] columns) throws SQLException {
        setColumns(preparedStatement, columns);
        return bindingToUser(preparedStatement.executeQuery());
    }

    private void setColumns(final PreparedStatement preparedStatement, final Object[] columns) throws SQLException {
        int parameterIndex = 1;
        for (Object column : columns) {
            preparedStatement.setObject(parameterIndex, column);
        }
    }

    private User bindingToUser(final ResultSet resultSet) throws SQLException {
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
