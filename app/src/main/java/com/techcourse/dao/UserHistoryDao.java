package com.techcourse.dao;

import com.techcourse.domain.UserHistory;
import com.interface21.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Objects;

public class UserHistoryDao {

    private static final String INSERT_SQL = "insert into user_history "
            + "(user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate);
    }

    public void log(final UserHistory history) {
        jdbcTemplate.update(INSERT_SQL, values(history));
    }

    private Object[] values(final UserHistory history) {
        return new Object[]{history.getUserId(), history.getAccount(), history.getPassword(),
                history.getEmail(), history.getCreatedAt(), history.getCreateBy()};
    }
}
