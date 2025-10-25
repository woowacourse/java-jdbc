package com.techcourse.dao;

import com.interface21.jdbc.mapper.RowMapper;
import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper {

    @Override
    public User mapRowToResult(final ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("ID"),
                rs.getString("ACCOUNT"),
                rs.getString("PASSWORD"),
                rs.getString("EMAIL")
        );
    }
}
