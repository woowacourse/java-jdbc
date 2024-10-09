package com.techcourse.dao.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;

public class UserRowMapper implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("account"),
                resultSet.getString("password"),
                resultSet.getString("email")
        );
    }
}
