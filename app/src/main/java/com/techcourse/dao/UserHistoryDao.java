package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public UserHistoryDao(final DataSource dataSource) {
        this(new JdbcTemplate(dataSource));
    }

    public void log(UserHistory userHistory) {
        String sql = "INSERT INTO user_history (user_id, account, password, email, created_at, created_by) VALUES (?, ?, ?, ?, ?, ?)";
        log.debug("query : {}", sql);

        jdbcTemplate.update(
                sql,
                userHistory.getUserId(), userHistory.getAccount(), userHistory.getPassword(),
                userHistory.getEmail(), userHistory.getCreatedAt(), userHistory.getCreateBy()
        );
    }

    public UserHistory findById(Long id) {
        String sql = "SELECT id, user_id, account, password, email, created_at, created_by FROM user_history WHERE id = ?";
        log.debug("query : {}", sql);

        return jdbcTemplate.queryForObject(sql, this::rowMapper, id);
    }

    private UserHistory rowMapper(ResultSet rs) {
        try {
            return new UserHistory(
                    rs.getLong("id"),
                    rs.getLong("user_id"),
                    rs.getString("account"),
                    rs.getString("password"),
                    rs.getString("email"),
                    rs.getString("created_by")
            );
        } catch (SQLException e) {
            throw new IllegalStateException("쿼리 실행 결과가 User 형식과 일치하지 않습니다.", e);
        }
    }
}
