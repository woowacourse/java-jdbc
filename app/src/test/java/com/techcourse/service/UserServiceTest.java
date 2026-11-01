package com.techcourse.service;

import com.techcourse.config.DataSourceConfig;
import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.domain.User;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

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
        final UserService userService = new UserService(userDao, userHistoryDao, dataSource);

        final String newPassword = "qqqqq";
        final String createBy = "gugu";
        final LocalDateTime before = LocalDateTime.now().minusSeconds(1);
        userService.changePassword(1L, newPassword, createBy);

        final User actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo(newPassword);
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from user_history where user_id = ? and password = ? and created_by = ? and created_at between ? and ?",
                row -> row.getLong(1), 1L, newPassword, createBy, before, LocalDateTime.now().plusSeconds(1)))
                .contains(1L);
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final UserHistoryDao userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final UserService userService = new UserService(userDao, userHistoryDao, dataSource);

        final String newPassword = "newPassword";
        final String createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        final User actual = userService.findById(1L);

        assertThat(actual.getPassword()).isEqualTo("password");
        assertThat(jdbcTemplate.queryForObject("select count(*) from user_history", row -> row.getLong(1)))
                .contains(0L);
    }

    @Test
    void testHistoryInsertFailure() {
        final UserService userService = new UserService(userDao, new UserHistoryDao(jdbcTemplate), dataSource);

        final DataAccessException exception = assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", null));

        assertThat(exception).hasCauseInstanceOf(SQLException.class);
        assertThat(userService.findById(1L).getPassword()).isEqualTo("password");
        assertThat(jdbcTemplate.queryForObject("select count(*) from user_history", row -> row.getLong(1)))
                .contains(0L);
    }

    @Test
    void testRollbackBeforeClose() throws SQLException {
        final Connection connection = spy(dataSource.getConnection());
        final DataSource transactionDataSource = mock(DataSource.class);
        when(transactionDataSource.getConnection()).thenReturn(connection);
        final UserService userService = new UserService(userDao, new MockUserHistoryDao(jdbcTemplate), transactionDataSource);

        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "gugu"));

        final InOrder order = inOrder(connection);
        order.verify(connection).setAutoCommit(false);
        order.verify(connection).rollback();
        order.verify(connection).setAutoCommit(true);
        order.verify(connection).close();
    }
}
