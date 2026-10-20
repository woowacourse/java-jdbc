package com.techcourse.repository;

import com.interface21.jdbc.core.EntityMapper;
import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper implements EntityMapper<User> {

    @Override
    public User map(final ResultSet resultSet) throws SQLException {
        return new User(
            resultSet.getLong(1),
            resultSet.getString(2),
            resultSet.getString(3),
            resultSet.getString(4));
    }
}
