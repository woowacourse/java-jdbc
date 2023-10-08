package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import org.springframework.jdbc.core.JdbcTemplate;

public class UserHistoryDao {
    private final JdbcTemplate template;

    public UserHistoryDao(final JdbcTemplate template) {
        this.template = template;
    }

    public void log(final UserHistory userHistory) {
        final String sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

        template.update(sql,
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy()
        );
    }
}
