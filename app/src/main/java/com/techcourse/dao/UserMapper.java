package com.techcourse.dao;

import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper implements RowMapper {

    @Override
    public User mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email")
        );
    }
}
