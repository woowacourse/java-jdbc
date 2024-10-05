package com.techcourse.dao.rowmapper;

import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRowMapper implements RowMapper<User> {


    @Override
    public User mapRow(ResultSet resultSet) {
        try {
            if (resultSet.next()) {
                return new User(
                        resultSet.getLong(1),
                        resultSet.getString(2),
                        resultSet.getString(3),
                        resultSet.getString(4));
            }
            return null;
        } catch (SQLException sqlException) {
            throw new RuntimeException("User를 mapping하는 과정에서 문제가 발생했습니다.", sqlException);
        }
    }
}