package com.techcourse.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.SQLException;
import java.util.UUID;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserHistoryDaoTest {

    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        final var jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setURL("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        dataSource = jdbcDataSource;
        DatabasePopulatorUtils.execute(dataSource);
    }

    @Test
    void logWithJdbcTemplate() throws SQLException {
        assertHistoryInserted(new UserHistoryDao(new JdbcTemplate(dataSource)));
    }

    @Test
    void logWithDataSource() throws SQLException {
        assertHistoryInserted(new UserHistoryDao(dataSource));
    }

    private void assertHistoryInserted(UserHistoryDao userHistoryDao) throws SQLException {
        userHistoryDao.log(new UserHistory(new User(1L, "gugu", "password", "gugu@example.com"), "admin"));

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement("select user_id, account, password, email, created_at, created_by from user_history");
             var rows = statement.executeQuery()) {
            assertThat(rows.next()).isTrue();
            assertThat(rows.getLong("user_id")).isEqualTo(1L);
            assertThat(rows.getString("account")).isEqualTo("gugu");
            assertThat(rows.getString("password")).isEqualTo("password");
            assertThat(rows.getString("email")).isEqualTo("gugu@example.com");
            assertThat(rows.getTimestamp("created_at")).isNotNull();
            assertThat(rows.getString("created_by")).isEqualTo("admin");
            assertThat(rows.next()).isFalse();
        }
    }
}
