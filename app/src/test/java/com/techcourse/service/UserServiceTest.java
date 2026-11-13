package com.techcourse.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import java.sql.SQLException;
import java.time.LocalDateTime;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        this.dataSource = DataSourceConfig.getInstance();
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(dataSource);
        jdbcTemplate.update("truncate table user_history restart identity");
        jdbcTemplate.update("truncate table users restart identity");
        final User user = new User("gugu", "password", "hkkang@woowahan.com");
        userDao.insert(user);
    }

    @Test
    void testChangePassword() {
        final UserHistoryDao userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final AppUserService appUserService = new AppUserService(userDao, userHistoryDao);
        final UserService userService = new TxUserService(appUserService, dataSource);

        final String newPassword = "qqqqq";
        final String createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        final User actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
        final SavedHistory history = jdbcTemplate.queryForObject(
                "select password, created_by, created_at from user_history where user_id = ?",
                rs -> new SavedHistory(
                        rs.getString("password"),
                        rs.getString("created_by"),
                        rs.getObject("created_at", LocalDateTime.class)
                ),
                1L
        );
        assertThat(history).isNotNull();
        assertThat(history.password()).isEqualTo(newPassword);
        assertThat(history.createdBy()).isEqualTo(createBy);
        assertThat(history.createdAt()).isNotNull();
    }

    @Test
    void testChangePasswordUsesOneConnectionForLookupAndWrites() throws SQLException {
        final DataSource observedDataSource = spy(dataSource);
        final JdbcTemplate sharedJdbcTemplate = new JdbcTemplate(observedDataSource);
        final AppUserService appUserService = new AppUserService(
                new UserDao(sharedJdbcTemplate),
                new UserHistoryDao(sharedJdbcTemplate)
        );
        final UserService userService = new TxUserService(appUserService, observedDataSource);

        userService.changePassword(1L, "newPassword", "gugu");

        verify(observedDataSource).getConnection();
        assertThat(userDao.findById(1L).getPassword()).isEqualTo("newPassword");
        assertThat(historyCount()).isEqualTo(1L);
    }

    @Test
    void testTransactionRollback() {
        final UserHistoryDao userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final AppUserService appUserService = new AppUserService(userDao, userHistoryDao);
        final UserService userService = new TxUserService(appUserService, dataSource);

        final String newPassword = "newPassword";
        final String createBy = "gugu";
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final User actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo("password");
        assertThat(historyCount()).isZero();
    }

    @Test
    void testTransactionRollbackAfterHistoryIsSaved() {
        final UserHistoryDao userHistoryDao = new UserHistoryDao(jdbcTemplate) {
            @Override
            public void log(final UserHistory userHistory) {
                super.log(userHistory);
                throw new DataAccessException();
            }
        };
        final AppUserService appUserService = new AppUserService(userDao, userHistoryDao);
        final UserService userService = new TxUserService(appUserService, dataSource);

        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        assertThat(userService.findById(1L).getPassword()).isEqualTo("password");
        assertThat(historyCount()).isZero();
    }

    @Test
    void testTransactionRollbackWhenHistoryInsertFails() {
        final UserHistoryDao userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final AppUserService appUserService = new AppUserService(userDao, userHistoryDao);
        final UserService userService = new TxUserService(appUserService, dataSource);

        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", null));

        assertThat(userService.findById(1L).getPassword()).isEqualTo("password");
        assertThat(historyCount()).isZero();
    }

    private long historyCount() {
        return jdbcTemplate.queryForObject(
                "select count(*) from user_history where user_id = ?", rs -> rs.getLong(1), 1L);
    }

    private record SavedHistory(String password, String createdBy, LocalDateTime createdAt) {
    }
}
