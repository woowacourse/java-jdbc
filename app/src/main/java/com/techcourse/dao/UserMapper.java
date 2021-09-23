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
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4)
                );
            }
        }

        return null;
    }
}