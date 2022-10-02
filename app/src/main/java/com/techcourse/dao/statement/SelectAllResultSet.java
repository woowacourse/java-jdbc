package com.techcourse.dao.statement;

import com.techcourse.domain.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import nextstep.jdbc.AbstractResultSetExecutor;

public class SelectAllResultSet extends AbstractResultSetExecutor<List<User>> {

    @Override
    protected List<User> executeQuery(final PreparedStatement preparedStatement,
                                      final Object[] columns) throws SQLException {
        return bindingToUsers(preparedStatement.executeQuery());
    }

    private List<User> bindingToUsers(final ResultSet resultSet) throws SQLException {
        List<User> users = new ArrayList<>();
        while (resultSet.next()) {
            long id = resultSet.getLong("id");
            String account = resultSet.getString("account");
            String password = resultSet.getString("password");
            String email = resultSet.getString("email");
            users.add(new User(id, account, password, email));
        }
        return users;
    }
}
