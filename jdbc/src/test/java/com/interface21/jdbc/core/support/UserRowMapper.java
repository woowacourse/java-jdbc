package com.interface21.jdbc.core.support;

import com.interface21.jdbc.core.RowMapper;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("name")
        );
    }
}
