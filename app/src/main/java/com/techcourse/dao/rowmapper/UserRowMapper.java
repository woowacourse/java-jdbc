package com.techcourse.dao.rowmapper;

import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper<User> {

    @Override
    public User mapRow(ResultSet resultSet) {
        try {
            return new User(
                    resultSet.getLong("id"),
                    resultSet.getString("account"),
                    resultSet.getString("password"),
                    resultSet.getString("email"));
        } catch (SQLException sqlException) {
            throw new RuntimeException("User를 mapping하는 과정에서 문제가 발생했습니다.", sqlException);
        }
    }
}
