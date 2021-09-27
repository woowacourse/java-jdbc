package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import nextstep.jdbc.RowMapper;

public class UserMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet rs) throws SQLException {
        try (rs) {
            if (rs.next()) {
                return new User(
                    rs.getLong("id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email")
                );
            }
        }

        return null;
    }
}