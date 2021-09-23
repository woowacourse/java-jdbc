package com.techcourse.dao;

import com.techcourse.domain.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import nextstep.jdbc.RowMapper;

public class UsersRowMapper implements RowMapper<List<User>> {

    @Override
    public List<User> mapRow(ResultSet rs) throws SQLException {
        List<User> results = new ArrayList<>();

        try (rs) {
            while (rs.next()) {
                results.add(new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4)
                ));
            }
        }

        return results;
    }
}