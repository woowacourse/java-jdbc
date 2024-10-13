package com.techcourse.dao.mapper;

import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.UserHistory;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserHistoryRowMapper implements RowMapper<UserHistory> {

    @Override
    public UserHistory mapRow(ResultSet rs) throws SQLException {
        return new UserHistory(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getString("created_by")
        );
    }
}
