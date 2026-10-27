package com.techcourse.dao;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;

class DaoTransactionTest {

    private UserDao userDao;
    private UserHistoryDao userHistoryDao;

    @BeforeEach
    void setUp() {
        final var dataSource = DataSourceConfig.getInstance();
        DatabasePopulatorUtils.execute(dataSource);
        userDao = new UserDao(dataSource);
        userHistoryDao = new UserHistoryDao(dataSource);
        userDao.insert(new User("gugu", "password", "gugu@example.com"));
    }

    @Test
    void callerCanRollbackPasswordAndHistoryTogether() throws SQLException {
        changePasswordAndFinishTransaction(false);
        assertThat(userDao.findById(1L).getPassword()).isEqualTo("password");
        assertHistoryCount(0);
    }

    @Test
    void callerCanCommitPasswordAndHistoryTogether() throws SQLException {
        changePasswordAndFinishTransaction(true);
        assertThat(userDao.findById(1L).getPassword()).isEqualTo("newPassword");
        assertHistoryCount(1);
    }

    private void changePasswordAndFinishTransaction(final boolean commit) throws SQLException {
        try (final var connection = DataSourceConfig.getInstance().getConnection()) {
            connection.setAutoCommit(false);
            final var user = userDao.findById(connection, 1L);
            user.changePassword("newPassword");
            userDao.update(connection, user);
            userHistoryDao.log(connection, new UserHistory(user, "gugu"));

            assertThat(connection.isClosed()).isFalse();
            assertThat(userDao.findById(connection, 1L).getPassword()).isEqualTo("newPassword");
            if (commit) {
                connection.commit();
            } else {
                connection.rollback();
            }
        }
    }

    private void assertHistoryCount(final int expected) throws SQLException {
        try (final var connection = DataSourceConfig.getInstance().getConnection();
             final var statement = connection.prepareStatement("select count(*) from user_history");
             final var resultSet = statement.executeQuery()) {
            resultSet.next();
            assertThat(resultSet.getInt(1)).isEqualTo(expected);
        }
    }
}
