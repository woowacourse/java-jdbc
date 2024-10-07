package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcException;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper {

    public User mapRow(final ResultSet resultSet) {
        try {
            return new User(
                    resultSet.getLong("id"),
                    resultSet.getString("account"),
                    resultSet.getString("password"),
                    resultSet.getString("email"));
        } catch (SQLException e) {
            throw new JdbcException("An error occurred during mapping row into object.", e);
        }
    }
}
