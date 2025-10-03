package com.techcourse.dao;

import com.interface21.jdbc.core.NamedParameterJdbcTemplate;
import com.techcourse.domain.UserHistory;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public UserHistoryDao(final DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public void log(final UserHistory userHistory) {
        final String sql = "insert into user_history (user_id, account, password, email, created_at, created_by) " +
                "values (:user_id, :account, :password, :email, :created_at, :created_by)";

        final Map<String, Object> params = new HashMap<>();
        params.put("user_id", userHistory.getUserId());
        params.put("account", userHistory.getAccount());
        params.put("password", userHistory.getPassword());
        params.put("email", userHistory.getEmail());
        params.put("created_at", userHistory.getCreatedAt());
        params.put("created_by", userHistory.getCreateBy());

        jdbcTemplate.update(sql, params);
    }
}
