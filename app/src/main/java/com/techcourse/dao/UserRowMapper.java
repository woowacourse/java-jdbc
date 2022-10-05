package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import nextstep.jdbc.RowMapper;

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
