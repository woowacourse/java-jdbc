package com.techcourse.service;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.support.TransactionSynchronizationManager;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserServiceTest {

    private EmbeddedDatabase dataSource;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        dataSource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .generateUniqueName(true)
                .addScript("schema.sql")
                .build();
        jdbcTemplate = new JdbcTemplate(dataSource);
        userDao = new UserDao(jdbcTemplate);
        userDao.insert(new User("gugu", "password", "hkkang@woowahan.com"));
    }

    @AfterEach
    void tearDown() {
        dataSource.shutdown();
    }

    @Test
    void testChangePassword() {
        final var userService = transactionalService(new UserHistoryDao(jdbcTemplate));

        userService.changePassword(1L, "qqqqq", "gugu");

        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        assertThat(userService.findById(1L).getPassword()).isEqualTo("qqqqq");
        final Integer historyCount = jdbcTemplate.queryForObject(
                "select count(*) from user_history where user_id = ? and password = ? and created_by = ?",
                (resultSet, rowNum) -> resultSet.getInt(1), 1L, "qqqqq", "gugu");
        assertThat(historyCount).isEqualTo(1);
    }

    @Test
    void testTransactionRollback() {
        final var userService = transactionalService(new MockUserHistoryDao(jdbcTemplate));

        final var failure = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(failure).hasMessage("Failed after saving password history");
        assertPasswordAndHistoryRolledBack(userService);
    }

    @Test
    void rollsBackPasswordWhenHistoryInsertFails() {
        final var userService = transactionalService(new UserHistoryDao(jdbcTemplate));

        final var failure = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "a".repeat(101)));

        assertThat(failure).hasCauseInstanceOf(SQLException.class);
        assertPasswordAndHistoryRolledBack(userService);
    }

    private UserService transactionalService(final UserHistoryDao historyDao) {
        return new TxUserService(dataSource, new AppUserService(userDao, historyDao));
    }

    private void assertPasswordAndHistoryRolledBack(final UserService userService) {
        assertThat(TransactionSynchronizationManager.getResource(dataSource)).isNull();
        assertThat(userService.findById(1L).getPassword()).isEqualTo("password");
        final Integer historyCount = jdbcTemplate.queryForObject("select count(*) from user_history",
                (resultSet, rowNum) -> resultSet.getInt(1));
        assertThat(historyCount).isZero();
    }
}
