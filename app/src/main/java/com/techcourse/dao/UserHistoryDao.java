package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.UserHistory;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserHistoryDao(DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public void log(UserHistory userHistory) {
        String sql = """
                INSERT INTO user_history (user_id, account, password, email, created_at, created_by) 
                values (?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                sql,
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy()
        );
    }

    public UserHistory findById(Long id) {
        String sql = "SELECT id, user_id, account, password, email, created_by FROM user_history WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, getUserHistoryRowMapper(), id);
    }

    private RowMapper<UserHistory> getUserHistoryRowMapper() {
        return (rs) -> new UserHistory(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getString("created_by")
        );
    }
}
