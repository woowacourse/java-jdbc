package com.techcourse.dao;

import javax.sql.DataSource;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.PreparedStatementCallBack;
import com.interface21.transaction.support.JdbcTransaction;
import com.techcourse.domain.UserHistory;

public class UserHistoryDao {

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserHistoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void log(UserHistory userHistory, JdbcTransaction transaction) {
        String sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

        PreparedStatementCallBack callBack = pstmt -> {
            pstmt.setLong(1, userHistory.getUserId());
            pstmt.setString(2, userHistory.getAccount());
            pstmt.setString(3, userHistory.getPassword());
            pstmt.setString(4, userHistory.getEmail());
            pstmt.setObject(5, userHistory.getCreatedAt());
            pstmt.setString(6, userHistory.getCreateBy());
        };

        jdbcTemplate.update(sql, callBack, transaction);
    }
}
