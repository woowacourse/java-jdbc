package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class UserHistoryDaoTest {

    private JdbcTemplate jdbcTemplate;
    private UserHistoryDao userHistoryDao;
    private UserHistory userHistory;

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        jdbcTemplate.update("""
                truncate table user_history restart identity
                """);
        userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var user = new User(1L, "gugu", "password99", "hkkang@woowahan.com");
        userHistory = new UserHistory(user, "admin");
    }

    @Test
    void 사용자_정보와_변경_시각과_변경자를_저장한다() {
        userHistoryDao.log(userHistory);

        final String sql = """
                select user_id, account, password, email, created_at, created_by
                from user_history
                """;
        final LocalDateTime createdAt = jdbcTemplate.queryForObject(sql, resultSet -> {
            assertThat(resultSet.getLong("user_id")).isEqualTo(userHistory.getUserId());
            assertThat(resultSet.getString("account")).isEqualTo(userHistory.getAccount());
            assertThat(resultSet.getString("password")).isEqualTo(userHistory.getPassword());
            assertThat(resultSet.getString("email")).isEqualTo(userHistory.getEmail());
            assertThat(resultSet.getString("created_by")).isEqualTo(userHistory.getCreateBy());
            return resultSet.getTimestamp("created_at").toLocalDateTime();
        });

        assertThat(createdAt).isCloseTo(userHistory.getCreatedAt(), within(1, ChronoUnit.MICROS));
    }

    @Test
    void DataSource로_생성한_DAO도_이력을_저장한다() {
        final var dao = new UserHistoryDao(DataSourceConfig.getInstance());

        dao.log(userHistory);

        final Long historyCount = jdbcTemplate.queryForObject("""
                select count(*)
                from user_history
                """, resultSet -> resultSet.getLong(1));

        assertThat(historyCount).isEqualTo(1L);
    }

    @Test
    void 외부_연결로_이력을_저장하고_커밋하면_이력이_남는다() throws SQLException {
        try (final var connection = DataSourceConfig.getInstance().getConnection()) {
            connection.setAutoCommit(false);

            userHistoryDao.log(connection, userHistory);

            connection.commit();
        }

        final Long historyCount = jdbcTemplate.queryForObject("""
                select count(*)
                from user_history
                """, resultSet -> resultSet.getLong(1));

        assertThat(historyCount).isEqualTo(1L);
    }

    @Test
    void 외부_연결로_이력을_저장하고_롤백하면_이력이_남지_않는다() throws SQLException {
        try (final var connection = DataSourceConfig.getInstance().getConnection()) {
            connection.setAutoCommit(false);

            userHistoryDao.log(connection, userHistory);

            connection.rollback();
        }

        final Long historyCount = jdbcTemplate.queryForObject("""
                select count(*)
                from user_history
                """, resultSet -> resultSet.getLong(1));

        assertThat(historyCount).isZero();
    }
}
