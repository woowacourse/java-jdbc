package com.techcourse.dao.mapper;

import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.UserHistory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class UserHistoryMapper implements RowMapper<UserHistory> {

    @Override
    public UserHistory map(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        long userId = rs.getLong("user_id");
        String account = rs.getString("account");
        String password = rs.getString("password");
        String email = rs.getString("email");
        Timestamp createdAt = rs.getTimestamp("createdAt");
        String createBy = rs.getString("createBy");
        return new UserHistory(id, userId, account, password, email, createdAt.toLocalDateTime(), createBy);
    }
}
