package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.domain.UserHistory;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        final var h2DataSource = new JdbcDataSource();
        h2DataSource.setURL(
                "jdbc:h2:mem:service-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1"
        );
        h2DataSource.setUser("sa");
        h2DataSource.setPassword("");

        dataSource = h2DataSource;
        DatabasePopulatorUtils.execute(dataSource);

        jdbcTemplate = new JdbcTemplate(dataSource);
        userDao = new UserDao(jdbcTemplate);

        userDao.insert(new User(
                "gugu",
                "password",
                "hkkang@woowahan.com"
        ));
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);

        final UserService userService = new TxUserService(
                new AppUserService(userDao, userHistoryDao),
                dataSource
        );

        final var user = userDao.findByAccount("gugu");

        userService.changePassword(user.getId(), "qqqqq", "gugu");

        final var actual = userService.findById(user.getId());
        assertThat(actual.getPassword()).isEqualTo("qqqqq");

        final var historyCount = jdbcTemplate.queryForObject(
                """
                select count(*) from user_history
                where user_id = ?
                  and password = ?
                  and created_by = ?
                """,
                rs -> rs.getInt(1),
                user.getId(),
                "qqqqq",
                "gugu"
        );

        assertThat(historyCount).isEqualTo(1);
    }

    @Test
    void testTransactionRollback() {
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);

        final UserService userService = new TxUserService(
                new AppUserService(userDao, userHistoryDao),
                dataSource
        );

        final var user = userDao.findByAccount("gugu");
        final var originalPassword = user.getPassword();

        assertThrows(
                DataAccessException.class,
                () -> userService.changePassword(
                        user.getId(),
                        "newPassword",
                        "gugu"
                )
        );

        final var actual = userService.findById(user.getId());

        assertThat(actual.getPassword()).isEqualTo(originalPassword);

        final var historyCount = jdbcTemplate.queryForObject(
                "select count(*) from user_history",
                rs -> rs.getInt(1)
        );

        assertThat(historyCount).isZero();
    }

    @Test
    void testTransactionRollbackOnError() throws SQLException {
        final var connection = spy(dataSource.getConnection());
        final var transactionDataSource = mock(DataSource.class);

        when(transactionDataSource.getConnection()).thenReturn(connection);

        final var transactionJdbcTemplate = new JdbcTemplate(transactionDataSource);
        final var transactionUserDao = new UserDao(transactionJdbcTemplate);

        final var failingHistoryDao = new UserHistoryDao(transactionJdbcTemplate) {
            @Override
            public void log(UserHistory userHistory) {
                throw new AssertionError("의도적으로 이력 저장 실패");
            }
        };

        final UserService userService = new TxUserService(
                new AppUserService(transactionUserDao, failingHistoryDao),
                transactionDataSource
        );

        final var user = userDao.findByAccount("gugu");
        final var originalPassword = user.getPassword();

        final var error = assertThrows(
                AssertionError.class,
                () -> userService.changePassword(
                        user.getId(),
                        "newPassword",
                        "gugu"
                )
        );

        assertThat(error).hasMessage("의도적으로 이력 저장 실패");

        final var order = inOrder(connection);
        order.verify(connection).rollback();
        order.verify(connection).close();

        assertThat(userDao.findById(user.getId()).getPassword())
                .isEqualTo(originalPassword);

        final var historyCount = jdbcTemplate.queryForObject(
                "select count(*) from user_history",
                rs -> rs.getInt(1)
        );

        assertThat(historyCount).isZero();
    }
}
