package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;

public class UserMapper implements RowMapper {

    @Override
    public Object map(final ResultSet rs) throws SQLException {
        return new User(
                rs.getLong(1),
                rs.getString(2),
                rs.getString(3),
                rs.getString(4));
    }
}
