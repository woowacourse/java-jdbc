package com.techcourse.dao.mapper;

import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper implements RowMapper<User> {

    @Override
    public User map(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String account = rs.getString("account");
        String password = rs.getString("password");
        String email = rs.getString("email");
        return new User(id, account, password, email);
    }
}
