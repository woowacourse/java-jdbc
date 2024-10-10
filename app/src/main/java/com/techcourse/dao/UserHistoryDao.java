package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.UserHistory;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHistoryDao {

    private static final Logger log = LoggerFactory.getLogger(UserHistoryDao.class);

    private final JdbcTemplate jdbcTemplate;

    public UserHistoryDao(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public UserHistoryDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int log(UserHistory userHistory) {
        String sql = "insert into user_history (user_id, account, password, email, created_at, created_by) values (?, ?, ?, ?, ?, ?)";
        int rowCount = jdbcTemplate.executeUpdate(sql, preparedStatement -> {
            preparedStatement.setObject(1, userHistory.getUserId());
            preparedStatement.setObject(2, userHistory.getAccount());
            preparedStatement.setObject(3, userHistory.getPassword());
            preparedStatement.setObject(4, userHistory.getEmail());
            preparedStatement.setObject(5, userHistory.getCreatedAt());
            preparedStatement.setObject(6, userHistory.getCreateBy());
        });
        log.debug("insert 성공한 row 개수 : {}", rowCount);
        return rowCount;
    }
}
