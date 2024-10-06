package com.techcourse.dao.rowmapper;

import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.UserHistory;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class UserHistoryRowMapper implements RowMapper<UserHistory> {

    @Override
    public UserHistory mapRow(ResultSet resultSet) {
        try {
            return new UserHistory(
                    resultSet.getLong("id"),
                    resultSet.getLong("user_id"),
                    resultSet.getString("account"),
                    resultSet.getString("password"),
                    resultSet.getString("email"),
                    resultSet.getObject("created_at", LocalDateTime.class),
                    resultSet.getString("created_by")
            );
        } catch (SQLException sqlException) {
            throw new RuntimeException("UserHistory를 mapping하는 과정에서 문제가 발생했습니다.", sqlException);
        }
    }
}
