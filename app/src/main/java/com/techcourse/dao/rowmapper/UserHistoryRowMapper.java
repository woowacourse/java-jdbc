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
                    resultSet.getLong(1),
                    resultSet.getLong(2),
                    resultSet.getString(3),
                    resultSet.getString(4),
                    resultSet.getString(5),
                    resultSet.getObject(6, LocalDateTime.class),
                    resultSet.getString(7)
            );
        } catch (SQLException sqlException) {
            throw new RuntimeException("UserHistory를 mapping하는 과정에서 문제가 발생했습니다.", sqlException);
        }
    }
}
