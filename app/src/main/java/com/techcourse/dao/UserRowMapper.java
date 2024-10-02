package com.techcourse.dao;

import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper {

    public User mapRow(final ResultSet resultSet) {
        try {
            return new User(
                    resultSet.getLong(1),
                    resultSet.getString(2),
                    resultSet.getString(3),
                    resultSet.getString(4));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
