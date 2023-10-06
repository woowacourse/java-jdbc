package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.Mapper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserHistoryDaoTest {

    private static final Mapper<UserHistory> USER_HISTORY_MAPPER = rs -> new UserHistory(
            rs.getLong("id"),
            rs.getLong("user_id"),
            rs.getString("account"),
            rs.getString("password"),
            rs.getString("email"),
            rs.getString("created_by")
    );

    private UserHistoryDao userHistoryDao;
    private JdbcTemplate jdbcTemplate;
    private DataSource dataSource;
    private User user;
    private UserHistory userHistory;

    @BeforeEach
    void setUp() {
        dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);

        jdbcTemplate = new JdbcTemplate(dataSource);
        userHistoryDao = new UserHistoryDao(jdbcTemplate);

        user = new User(1L, "gugu", "password", "email");
        userHistory = new UserHistory(user, "gugu");

        jdbcTemplate.update("DELETE FROM user_history");
    }

    @Test
    void 로그를_저장한다() {
        // given
        // when
        userHistoryDao.log(userHistory);

        // then
        final List<UserHistory> actual = jdbcTemplate.query("select * from user_history", USER_HISTORY_MAPPER);

        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("id", "createdAt")
                .isEqualTo(List.of(userHistory));
    }

    @Test
    void 커밋하면_로그를_저장한다() throws SQLException {
        // given
        final Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);

        // when
        userHistoryDao.log(connection, userHistory);

        connection.commit();

        // then
        final List<UserHistory> actual = jdbcTemplate.query("select * from user_history", USER_HISTORY_MAPPER);

        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("id", "createdAt")
                .isEqualTo(List.of(userHistory));
    }

    @Test
    void 커밋하지_않으면_로그가_저장되지_않는() throws SQLException {
        // given
        final Connection connection = dataSource.getConnection();
        connection.setAutoCommit(false);

        // when
        userHistoryDao.log(connection, userHistory);

        // then
        final List<UserHistory> actual = jdbcTemplate.query("select * from user_history", USER_HISTORY_MAPPER);

        assertThat(actual)
                .isEmpty();
    }
}
