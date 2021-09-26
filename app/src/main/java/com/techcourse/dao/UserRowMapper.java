package com.techcourse.dao;

import com.techcourse.domain.User;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper {

    @Override
    public Object mapRow(ResultSet rs) throws SQLException {
        if (rs.next()) {
            return new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4));
        }
        return null;
    }
}
