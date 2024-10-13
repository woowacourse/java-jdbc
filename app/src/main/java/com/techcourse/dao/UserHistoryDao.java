package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.domain.UserHistory;
import java.sql.Connection;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private static final RowMapper<UserHistory> userHistoryRowMapper = (rs) -> new UserHistory(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email"),
            rs.getString("created_by")
    );

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserHistoryDao(DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public void log(Connection conn, UserHistory userHistory) {
        String sql = """
                INSERT INTO user_history (user_id, account, password, email, created_at, created_by) 
                values (?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                conn,
                sql,
                userHistory.getUserId(),
                userHistory.getAccount(),
                userHistory.getPassword(),
                userHistory.getEmail(),
                userHistory.getCreatedAt(),
                userHistory.getCreateBy()
        );
    }

    public UserHistory findById(Connection conn, Long id) {
        String sql = "SELECT id, user_id, account, password, email, created_by FROM user_history WHERE id = ?";
        return jdbcTemplate.queryForObject(conn, sql, userHistoryRowMapper, id);
    }
}
