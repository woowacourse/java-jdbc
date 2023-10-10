package org.springframework.jdbc.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.exception.DataNotFoundException;
import org.springframework.jdbc.core.test_supporter.DataSourceConfig;
import org.springframework.jdbc.core.test_supporter.DatabasePopulatorUtils;
import org.springframework.jdbc.core.test_supporter.User;
import org.springframework.jdbc.core.test_supporter.UserDao;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class JdbcTemplateTest {

    private static final User USER_FIXTURE = new User("hong-sile", "hong", "hong@teco.com");
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceConfig.getInstance());
    private final UserDao userDao = new UserDao(DataSourceConfig.getInstance());

    @BeforeEach
    void setUp() {
        DatabasePopulatorUtils.execute(DataSourceConfig.getInstance());
    }

    @Test
    @DisplayName("execute Query로 읽는 쿼리를 실행할 수 있다.")
    void executeQuery() {
        userDao.insert(USER_FIXTURE);
        final String sql = "select id, account, password, email from users where id = ?";
        final Long id = 1L;

        final User actual = jdbcTemplate.executeQuery(sql, (rs) ->
                new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4)
                )
            , id);

        assertAll(
            () -> assertThat(id)
                .isEqualTo(actual.getId()),
            () -> assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(USER_FIXTURE)
        );
    }

    @Test
    @DisplayName("execute로 쓰는 쿼리를 실행할 수 있다.")
    void execute() {
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        final Long id = 1L;

        jdbcTemplate.executeUpdate(
            sql,
            USER_FIXTURE.getAccount(), USER_FIXTURE.getPassword(), USER_FIXTURE.getEmail()
        );

        final User actual = userDao.findById(id);
        assertAll(
            () -> assertThat(id)
                .isEqualTo(actual.getId()),
            () -> assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(USER_FIXTURE)
        );
    }

    @Test
    @DisplayName("data가 존재하지 않는경우 Exception을 throw한다.")
    void noDataFoundException() {
        final String sql = "select id, account, password, email from users where id = ?";
        final Long unvalidatedId = Long.MIN_VALUE;

        final ThrowingCallable testTarget = () -> jdbcTemplate.executeQuery(sql, (rs) ->
                new User(
                    rs.getLong(1),
                    rs.getString(2),
                    rs.getString(3),
                    rs.getString(4)
                ),
            unvalidatedId
        );

        assertThatThrownBy(testTarget)
            .isInstanceOf(DataNotFoundException.class);
    }

    @Nested
    @DisplayName("트랜잭션이 활성화 여부에 따라 Connection을 close한다.")
    class CloseNonTransactionConnection {

        private JdbcTemplate jdbcTemplate;
        private DataSource dataSource;
        private Connection connection;

        @BeforeEach
        void setUp() throws SQLException {
            dataSource = spy(DataSourceConfig.getInstance());
            connection = spy(dataSource.getConnection());
            when(dataSource.getConnection()).thenReturn(connection);
            jdbcTemplate = new JdbcTemplate(dataSource);
        }

        @Test
        @DisplayName("트랜잭션이 활성화되어 있지 않으면 close한다.")
        void closeNonTransactionConnection() throws SQLException {
            final String sql = "insert into users (account, password, email) values (?, ?, ?)";

            jdbcTemplate.executeUpdate(
                sql,
                USER_FIXTURE.getAccount(), USER_FIXTURE.getPassword(), USER_FIXTURE.getEmail()
            );
            verify(connection, times(1)).close();
        }

        @Test
        @DisplayName("트랜잭션이 활성화되어 있으면 close하지 않는다.")
        void closeTransactionConnection() throws SQLException {
            final String sql = "insert into users (account, password, email) values (?, ?, ?)";
            final Connection connection = DataSourceUtils.getConnection(dataSource);
            connection.setAutoCommit(false);

            jdbcTemplate.executeUpdate(
                sql,
                USER_FIXTURE.getAccount(), USER_FIXTURE.getPassword(), USER_FIXTURE.getEmail()
            );

            verify(connection, never()).close();
            TransactionSynchronizationManager.clear();
            connection.close();
        }
    }
}
