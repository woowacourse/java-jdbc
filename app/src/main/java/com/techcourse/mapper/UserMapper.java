package com.techcourse.mapper;

import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;

public class UserMapper {
    public static final RowMapper<User> USER_ROW_MAPPER = rs -> new User(
            rs.getLong("id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email")
    );
}
