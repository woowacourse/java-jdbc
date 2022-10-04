package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private Connection connection;
    private DataSource dataSource;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws Exception {
        this.connection = mock(Connection.class);
        this.dataSource = mock(DataSource.class);
        this.preparedStatement = mock(PreparedStatement.class);
        this.resultSet = mock(ResultSet.class);
        this.jdbcTemplate = new JdbcTemplate(this.dataSource);

        given(this.dataSource.getConnection()).willReturn(this.connection);
        given(this.connection.prepareStatement(anyString())).willReturn(this.preparedStatement);
        given(this.preparedStatement.executeQuery()).willReturn(this.resultSet);
        given(this.preparedStatement.executeQuery(anyString())).willReturn(this.resultSet);
        given(this.preparedStatement.getConnection()).willReturn(this.connection);
    }

    @DisplayName("query 실행 후, Connection과 PreparedStatement는 close 된다")
    @Test
    void close_connection_and_preparedStatement_after_query() throws SQLException {
        // given
        final var count = new AtomicInteger(1);
        given(resultSet.next()).will(invocation -> {
            count.getAndDecrement();
            return count.get() == 0;
        });
        final var column = "account";
        final var expected = "gugu";
        given(resultSet.getString(column)).willReturn(expected);

        // when
        final var actual = jdbcTemplate.queryForOne(
                "select account from users where id = ?",
                rs -> rs.getString(column),
                1L
        );

        // then
        assertAll(
                () -> verify(preparedStatement, times(1)).setObject(1, 1L),
                () -> verify(resultSet, times(1)).getString(column),
                () -> assertThat(actual).isEqualTo(expected),
                () -> verify(connection, times(1)).close(),
                () -> verify(preparedStatement, times(1)).close()
        );
    }

    @DisplayName("command 실행 시, 전달된 매개변수를 세팅하고, 완료 후 connection과 PreparedStatement는 close된다")
    @Test
    void close_connection_and_preparedStatement_after_command() {
        // given
        final var userName = "userName";
        final var password = "password";
        final var email = "hkkang@woowahan.com";
        final var user = new User(1L, userName, password, email);

        // when
        jdbcTemplate.command(
                "insert into users (account, password, email) values (?, ?, ?)",
                user.getAccount(), user.getPassword(), user.getEmail()
        );

        // then
        assertAll(
                () -> verify(preparedStatement, times(1)).setObject(1, userName),
                () -> verify(preparedStatement, times(1)).setObject(2, password),
                () -> verify(preparedStatement, times(1)).setObject(3, email),
                () -> verify(connection, times(1)).close(),
                () -> verify(preparedStatement, times(1)).close()
        );
    }

    @DisplayName("queryForList 는 List<T> 로 응답한다")
    @Test
    void queryForList_should_return_List_and_close_connection_and_preparedStatement() throws SQLException {
        // given
        final var count = new AtomicInteger(3);
        given(resultSet.next()).will(invocation -> {
            count.getAndDecrement();
            return count.get() >= 0;
        });
        final var column = "account";
        final var expected = "gugu";
        given(resultSet.getString(column)).willReturn(expected);

        // when
        final var actual = jdbcTemplate.queryForList(
                "select account from users",
                rs -> rs.getString(column)
        );

        // then
        assertAll(
                () -> verify(resultSet, times(3)).getString(column),
                () -> assertThat(actual).isEqualTo(List.of(expected, expected, expected)),
                () -> verify(connection, times(1)).close(),
                () -> verify(preparedStatement, times(1)).close()
        );
    }

    @DisplayName("queryForOne 메서드는 결과 행이 없으면 예외를 던진다")
    @Test
    void queryForOne_should_throw_exception_when_no_resultSet_exists() throws SQLException {
        // given
        final var count = new AtomicInteger(3);
        given(resultSet.next()).will(invocation -> {
            count.getAndDecrement();
            return count.get() >= 0;
        });
        final var column = "account";
        final var expected = "gugu";
        given(resultSet.getString(column)).willReturn(expected);

        // when & then
        assertAll(
                () -> assertThatThrownBy(() ->
                        jdbcTemplate.queryForOne(
                                "select account from users",
                                rs -> rs.getString("account")
                        )
                ).isInstanceOf(DataAccessException.class)
                        .hasMessageContaining("Expected single result, but 3"),
                () -> verify(connection, times(1)).close(),
                () -> verify(preparedStatement, times(1)).close()
        );
    }

    @DisplayName("queryForOne 메서드는 결과 행이 2 이상이면 예외를 던진다")
    @Test
    void queryForOne_should_throw_exception_when_resultSet_has_two_or_greater_rows() throws SQLException {
        // given
        given(resultSet.next()).willReturn(false);

        // when & then
        assertAll(
                () -> assertThatThrownBy(() ->
                        jdbcTemplate.queryForOne(
                                "select account from users",
                                rs -> rs.getString("account")
                        )
                ).isInstanceOf(DataAccessException.class)
                        .hasMessageContaining("Expected single result, but 0"),
                () -> verify(connection, times(1)).close(),
                () -> verify(preparedStatement, times(1)).close()
        );
    }

    static class User {
        private final Long id;
        private final String account;
        private final String password;
        private final String email;

        public User(final Long id, final String account, final String password, final String email) {
            this.id = id;
            this.account = account;
            this.password = password;
            this.email = email;
        }

        public User(final String account, final String password, final String email) {
            this(null, account, password, email);
        }

        public Long getId() {
            return id;
        }

        public String getAccount() {
            return account;
        }

        public String getPassword() {
            return password;
        }

        public String getEmail() {
            return email;
        }
    }
}
