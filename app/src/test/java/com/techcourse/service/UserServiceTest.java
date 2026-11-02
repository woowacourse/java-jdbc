package com.techcourse.service;

import com.techcourse.dao.UserDao;
import com.techcourse.dao.UserHistoryDao;
import com.techcourse.support.jdbc.init.DatabasePopulatorUtils;
import com.interface21.dao.DataAccessException;
import com.interface21.jdbc.core.JdbcTemplate;
import com.interface21.transaction.support.TransactionTemplate;
import java.sql.SQLException;
import java.util.UUID;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserServiceTest {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private TransactionTemplate transactionTemplate;
    private UserDao userDao;

    @BeforeEach
    void setUp() {
        final var dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:user-service-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1;");
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.transactionTemplate = new TransactionTemplate(dataSource);
        this.userDao = new UserDao(jdbcTemplate);

        DatabasePopulatorUtils.execute(dataSource);
        jdbcTemplate.update(
                "insert into users (account, password, email) values (?, ?, ?)",
                "gugu", "password", "hkkang@woowahan.com"
        );
    }

    @Test
    void testChangePassword() {
        final var userHistoryDao = new UserHistoryDao(jdbcTemplate);
        final var userService = new UserService(transactionTemplate, userDao, userHistoryDao);

        final var newPassword = "qqqqq";
        final var createBy = "gugu";
        userService.changePassword(1L, newPassword, createBy);

        assertThat(password()).isEqualTo(newPassword);
        final var historyCount = jdbcTemplate.queryForObject(
                "select count(*) from user_history where user_id = ? and password = ? and created_by = ? and created_at is not null",
                rs -> rs.getInt(1), 1L, newPassword, createBy
        );
        assertThat(historyCount).isEqualTo(1);
    }

    @Test
    void testTransactionRollback() {
        // 트랜잭션 롤백 테스트를 위해 mock으로 교체
        final var userHistoryDao = new MockUserHistoryDao(jdbcTemplate);
        final var userService = new UserService(transactionTemplate, userDao, userHistoryDao);

        final var originalPassword = password();
        final var newPassword = "newPassword";
        final var createBy = "gugu";
        // 트랜잭션이 정상 동작하는지 확인하기 위해 의도적으로 MockUserHistoryDao에서 예외를 발생시킨다.
        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, newPassword, createBy));

        assertThat(password()).isEqualTo(originalPassword);
        assertThat(historyCount()).isZero();
    }

    @Test
    void 이력_저장_SQL이_실패하면_비밀번호를_롤백한다() {
        final var userService = new UserService(transactionTemplate, userDao, new UserHistoryDao(jdbcTemplate));
        final var originalPassword = password();

        assertThrows(DataAccessException.class,
                () -> userService.changePassword(1L, "newPassword", "a".repeat(101)));

        assertThat(password()).isEqualTo(originalPassword);
        assertThat(historyCount()).isZero();
    }

    @Test
    void 조회부터_이력_저장까지_같은_연결을_사용한다() throws SQLException {
        try (final var actualConnection = dataSource.getConnection()) {
            final var connection = spy(actualConnection);
            final var transactionDataSource = mock(DataSource.class);
            when(transactionDataSource.getConnection()).thenReturn(connection);
            final var jdbcTemplate = new JdbcTemplate(transactionDataSource);
            final var userService = new UserService(
                    new TransactionTemplate(transactionDataSource),
                    new UserDao(jdbcTemplate),
                    new UserHistoryDao(jdbcTemplate)
            );

            userService.changePassword(1L, "newPassword", "gugu");

            verify(transactionDataSource).getConnection();
            final var transactionOrder = inOrder(connection);
            transactionOrder.verify(connection).setAutoCommit(false);
            transactionOrder.verify(connection).prepareStatement(startsWith("select"));
            transactionOrder.verify(connection).prepareStatement(startsWith("update"));
            transactionOrder.verify(connection).prepareStatement(startsWith("insert"));
            transactionOrder.verify(connection).commit();
            transactionOrder.verify(connection).setAutoCommit(true);
            transactionOrder.verify(connection).close();
        }
    }

    private String password() {
        return jdbcTemplate.queryForObject(
                "select password from users where id = ?", rs -> rs.getString("password"), 1L
        );
    }

    private int historyCount() {
        return jdbcTemplate.queryForObject("select count(*) from user_history", rs -> rs.getInt(1));
    }
}
