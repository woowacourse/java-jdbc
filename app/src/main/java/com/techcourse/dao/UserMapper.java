package com.techcourse.dao;

import com.techcourse.domain.User;
import nextstep.jdbc.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper implements RowMapper<User> {

    @Override
    public User apply(ResultSet resultSet) throws SQLException {
        if(resultSet.next()){
            return new User(
                    resultSet.getLong("id"),
                    resultSet.getString("account"),
                    resultSet.getString("password"),
                    resultSet.getString("email")
                    );
        }
        return null;
    }
}
