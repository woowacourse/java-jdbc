package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.jdbc.core.RowMapper;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class UserHistoryDaoTest {

    private UserHistoryDao userHistoryDao;
    private JdbcTemplate jdbcTemplate;
    private User user;

    private final RowMapper<UserHistory> rowMapper = resultSet -> {
        return new UserHistory(
                resultSet.getLong("id"),
                resultSet.getLong("user_id"),
                resultSet.getString("account"),
                resultSet.getString("password"),
                resultSet.getString("email"),
                resultSet.getString("created_by")
        );
    };

    @BeforeEach
    void setup() {
        DataSource dataSource = DataSourceConfig.getInstance();
        jdbcTemplate = new JdbcTemplate(dataSource);
        userHistoryDao = new UserHistoryDao(jdbcTemplate);
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        jdbcTemplate.update("truncate table users restart identity");
        jdbcTemplate.update("truncate table user_history restart identity");
        insertUser();
    }

    @Test
    void log() {
        // given
        String createdBy = "createdBy";
        UserHistory userHistory = new UserHistory(user, createdBy);

        // when
        userHistoryDao.log(userHistory);

        // then
        String sql = "select * from user_history where user_id = ?";
        UserHistory savedUserHistory = jdbcTemplate.queryForObject(sql, rowMapper, 1);
        assertThat(savedUserHistory.getCreateBy()).isEqualTo(createdBy);
    }

    @Test
    void logWithConnection() throws SQLException {
        // given
        String createdBy = "createdBy";
        UserHistory userHistory = new UserHistory(user, createdBy);

        // when
        Connection connection = DataSourceConfig.getInstance().getConnection();
        userHistoryDao.log(connection, userHistory);

        // then
        String sql = "select * from user_history where user_id = ?";
        UserHistory savedUserHistory = jdbcTemplate.queryForObject(sql, rowMapper, 1);
        assertThat(savedUserHistory.getCreateBy()).isEqualTo(createdBy);
    }

    private void insertUser() {
        user = new User(1, "gugu", "password", "hkkang@woowahan.com");
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, user.getAccount(), user.getPassword(), user.getEmail());
    }

}
