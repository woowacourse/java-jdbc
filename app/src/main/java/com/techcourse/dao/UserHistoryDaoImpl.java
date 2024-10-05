package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementSetter;
import com.techcourse.domain.UserHistory;
import javax.sql.DataSource;

public class UserHistoryDaoImpl implements UserHistoryDao {

    private JdbcTemplate jdbcTemplate;

    public UserHistoryDaoImpl(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserHistoryDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void log(final UserHistory userHistory) {
        String sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        PreparedStatementSetter setter = pstmt -> {
            pstmt.setObject(1, userHistory.getUserId());
            pstmt.setObject(2, userHistory.getAccount());
            pstmt.setObject(3, userHistory.getPassword());
            pstmt.setObject(4, userHistory.getEmail());
            pstmt.setObject(5, userHistory.getCreatedAt());
            pstmt.setObject(6, userHistory.getCreateBy());
        };

        jdbcTemplate.update(sql, setter);
    }
}
