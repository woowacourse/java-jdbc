package com.techcourse.dao.statement;

import com.techcourse.domain.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import nextstep.jdbc.ResultSetExecutor;

public class SelectAllResultSet implements ResultSetExecutor<User> {

    @Override
    public List<User> execute(final Connection connection) {
        final String sql = "select * from users";

        try (PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                String account = resultSet.getString("account");
                String password = resultSet.getString("password");
                String email = resultSet.getString("email");
                users.add(new User(id, account, password, email));
            }
            return users;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
