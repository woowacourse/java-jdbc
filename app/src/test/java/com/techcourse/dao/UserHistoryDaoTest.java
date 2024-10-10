package com.techcourse.dao;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserHistoryDaoTest {

    private JdbcTemplate jdbcTemplate;
    private UserHistoryDao userHistoryDao;
    private Connection connection;

    @BeforeEach
    void setup() throws SQLException {
        DataSource dataSource = DataSourceConfig.getInstance();
        connection = dataSource.getConnection();
        DatabasePopulatorUtils.execute(dataSource);

        userHistoryDao = new UserHistoryDao(DataSourceConfig.getInstance());
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
    }

    @DisplayName("회원의 기록을 저장한다.")
    @Test
    void log() {
        // given
        User user = new User(1L, "gugu", "password", "hkkang@woowahan.com");

        UserHistory userHistory = new UserHistory(user, "2024-01-01");

        // when
        userHistoryDao.log(connection, userHistory);

        // then
        String sql = """
                     SELECT id, user_id, account, password, email, created_at, created_by
                     FROM user_history
                     WHERE user_id = """ + user.getId();

        UserHistory findUserHistory = jdbcTemplate.queryForObject(sql, rs -> new UserHistory(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email"),
                rs.getString("created_by")
        ));

        assertAll(
                () -> assertEquals(findUserHistory.getId(), userHistory.getUserId()),
                () -> assertEquals(findUserHistory.getAccount(), userHistory.getAccount()),
                () -> assertEquals(findUserHistory.getPassword(), userHistory.getPassword()),
                () -> assertEquals(findUserHistory.getEmail(), userHistory.getEmail()),
                () -> assertEquals(findUserHistory.getCreateBy(), userHistory.getCreateBy())
        );
    }
}
