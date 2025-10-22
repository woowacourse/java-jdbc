package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.NamedJdbcTemplate;
import com.interface21.jdbc.core.NamedSqlParamMap;
import com.techcourse.domain.UserHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final NamedJdbcTemplate namedJdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.namedJdbcTemplate = new NamedJdbcTemplate(jdbcTemplate);
    }

    public void log(final UserHistory userHistory) {
        final var sql = """
            insert into user_history (user_id, account, password, email, created_at, created_by) 
            values (:user_id, :account, :password, :email, :created_at, :created_by)
            """;

        log.debug("user log insert : {}", userHistory);
        NamedSqlParamMap params = new NamedSqlParamMap()
            .addValue("user_id", userHistory.getUserId())
            .addValue("account", userHistory.getAccount())
            .addValue("password", userHistory.getPassword())
            .addValue("email", userHistory.getEmail())
            .addValue("created_at", userHistory.getCreatedAt())
            .addValue("created_by", userHistory.getCreateBy());
        namedJdbcTemplate.update(sql, params);
    }
}
