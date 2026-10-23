package com.techcourse.dao;

import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.SQLException;
import java.time.temporal.ChronoUnit;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class UserHistoryDaoTest {

    private JdbcDataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:history-dao-test;DB_CLOSE_DELAY=-1");
        DatabasePopulatorUtils.execute(dataSource);
        new JdbcTemplate(dataSource).update("truncate table user_history restart identity", new Object[0]);
    }

    @Test
    void logsHistoryWithJdbcTemplate() throws SQLException {
        assertHistorySaved(new UserHistoryDao(new JdbcTemplate(dataSource)));
    }

    @Test
    void logsHistoryWithDataSource() throws SQLException {
        assertHistorySaved(new UserHistoryDao(dataSource));
    }

    private void assertHistorySaved(UserHistoryDao dao) throws SQLException {
        final var user = new User(7L, "pino", "new-password", "pino@example.com");
        final var history = new UserHistory(user, "admin");

        dao.log(history);

        try (final var connection = dataSource.getConnection();
             final var statement = connection.prepareStatement("select * from user_history");
             final var resultSet = statement.executeQuery()) {
            assertThat(resultSet.next()).isTrue();
            assertThat(resultSet.getLong("user_id")).isEqualTo(user.getId());
            assertThat(resultSet.getString("account")).isEqualTo(user.getAccount());
            assertThat(resultSet.getString("password")).isEqualTo(user.getPassword());
            assertThat(resultSet.getString("email")).isEqualTo(user.getEmail());
            assertThat(resultSet.getTimestamp("created_at").toLocalDateTime())
                    .isCloseTo(history.getCreatedAt(), within(1, ChronoUnit.SECONDS));
            assertThat(resultSet.getString("created_by")).isEqualTo("admin");
            assertThat(resultSet.next()).isFalse();
        }
    }
}
