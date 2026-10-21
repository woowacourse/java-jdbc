package com.techcourse.repository;

import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsersRowMapper implements RowMapper<List<User>> {

    @Override
    public List<User> mapRow(final ResultSet resultSet) throws SQLException {
        final List<User> result = new ArrayList<>();
        while (resultSet.next()) {
            final User user = new User(
                resultSet.getLong("id"),
                resultSet.getString("account"),
                resultSet.getString("password"),
                resultSet.getString("email"));
            result.add(user);
        }
        return result;
    }
}
