package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
        userDao = new UserDao(jdbcTemplate);
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
        jdbcTemplate.update("truncate table user_history restart identity");
        jdbcTemplate.update("truncate table users restart identity");
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
    }

    @Test
    void testChangePassword() {
        final var service = service(new UserHistoryDao(jdbcTemplate));
        service.changePassword(1L, "qqqqq", "gugu");

        assertThat(service.findById(1L).getPassword()).isEqualTo("qqqqq");
        final var histories = jdbcTemplate.query("select * from user_history where user_id = ?", (rs, rowNum) -> {
            assertThat(rs.getLong("user_id")).isEqualTo(1L);
            assertThat(rs.getString("account")).isEqualTo("gugu");
            assertThat(rs.getString("password")).isEqualTo("qqqqq");
            assertThat(rs.getString("email")).isEqualTo("hkkang@woowahan.com");
            assertThat(rs.getString("created_by")).isEqualTo("gugu");
            assertThat(rs.getTimestamp("created_at")).isNotNull();
            return rs.getLong("id");
        }, 1L);
        assertThat(histories).hasSize(1);
    }

    @Test
    void testTransactionRollback() {
        final var service = service(new MockUserHistoryDao(jdbcTemplate));

        assertThrows(DataAccessException.class,
                () -> service.changePassword(1L, "newPassword", "gugu"));

        assertOriginalState(service);
    }

    @Test
    void rollsBackPasswordAndAlreadyInsertedHistory() {
        final var failure = new DataAccessException("failure after history insert");
        final var historyDao = new UserHistoryDao(jdbcTemplate) {
            @Override
            public void log(final Connection connection, final UserHistory history) {
                super.log(connection, history);
                throw failure;
            }
        };
        final var service = service(historyDao);

        assertThat(assertThrows(DataAccessException.class,
                () -> service.changePassword(1L, "newPassword", "gugu"))).isSameAs(failure);

        assertOriginalState(service);
    }

    private UserService service(final UserHistoryDao historyDao) {
        return new UserService(DataSourceConfig.getInstance(), userDao, historyDao);
    }

    private void assertOriginalState(final UserService service) {
        assertThat(service.findById(1L).getPassword()).isEqualTo("password");
        assertThat(jdbcTemplate.query("select id from user_history where user_id = ?",
                (rs, rowNum) -> rs.getLong("id"), 1L)).isEmpty();
    }
}
