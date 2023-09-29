package com.techcourse.dao;

import com.techcourse.domain.User;
import jakarta.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class UserRowMapper implements RowMapper<User> {

    @Nullable
    @Override
    public User mapRow(final ResultSet resultSet, final int rowNumber) throws SQLException {
        return new User(
            resultSet.getLong("id"),
            resultSet.getString("account"),
            resultSet.getString("password"),
            resultSet.getString("email")
        );
    }
}
