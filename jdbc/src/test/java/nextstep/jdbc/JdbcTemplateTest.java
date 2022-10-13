package nextstep.jdbc;

import static nextstep.jdbc.UserFixture.로마;
import static nextstep.jdbc.UserFixture.수달;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private Connection connection;
    private PreparedStatement statement;
    private DataSource dataSource;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        jdbcTemplate = new JdbcTemplate(dataSource);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(any())).willReturn(statement);
    }

    @Test
    @DisplayName("update 메서드는 쿼리를 실행한다.")
    void update() {
        // given & when
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, "account", "password", "email");

        // then
        assertAll(
                () -> verify(statement).setObject(1, "account"),
                () -> verify(statement).execute(),
                () -> verify(statement).close(),
                () -> verify(connection).close()
        );
    }

    @Test
    @DisplayName("update 메서드는 쿼리를 실행한다.(with TxManager)")
    void update_txManager() {
        // given & when
        final DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        final TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());

        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, "account", "password", "email");

        transactionManager.rollback(status);
        // then
        assertAll(
                () -> verify(statement).setObject(1, "account"),
                () -> verify(statement).execute(),
                () -> verify(statement).close(),
                () -> verify(connection).close()
        );
    }

    @Nested
    @DisplayName("queryForObject 메서드는 ")
    class QueryForObject {
        @Test
        @DisplayName("쿼리를 실행하고 단일객체를 반환한다.")
        void success() throws SQLException {
            // given
            final ResultSet resultSet = mock(ResultSet.class);

            given(statement.executeQuery()).willReturn(resultSet);
            given(resultSet.next()).willReturn(true, false);
            given(resultSet.getString("account")).willReturn(로마.getAccount());
            given(resultSet.getString("password")).willReturn(로마.getPassword());
            given(resultSet.getString("email")).willReturn(로마.getEmail());

            // when
            final String sql = "select account, password, email from users where account = ?";
            final User result = jdbcTemplate.queryForObject(sql, getRowMapper(), 로마.getAccount());

            // then
            assertAll(
                    () -> assertThat(result).isEqualTo(로마),
                    () -> verify(statement).setObject(1, 로마.getAccount()),
                    () -> verify(statement).executeQuery(),
                    () -> verify(statement).close(),
                    () -> verify(connection).close(),
                    () -> verify(resultSet).close()
            );
        }

        @Test
        @DisplayName("하나 이상의 쿼리 결과가 나오면 예외를 반환한다.")
        void queryForObject_moreThanOne_exception() throws SQLException {
            // given
            final ResultSet resultSet = mock(ResultSet.class);

            given(statement.executeQuery()).willReturn(resultSet);
            given(resultSet.next()).willReturn(true, true, false);
            given(resultSet.getString("account")).willReturn(로마.getAccount());
            given(resultSet.getString("password")).willReturn(로마.getPassword());
            given(resultSet.getString("email")).willReturn(로마.getEmail());

            // when
            final String sql = "select account, password, email from users where account = ?";

            // then
            assertAll(
                    () -> assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, getRowMapper(), "roma"))
                            .isInstanceOf(DataAccessException.class)
                            .hasMessage("more than one result!"),
                    () -> verify(statement).executeQuery(),
                    () -> verify(statement).close(),
                    () -> verify(connection).close(),
                    () -> verify(resultSet).close()
            );
        }

        @Test
        @DisplayName("쿼리 조회 결과가 없으면 예외를 반환한다.")
        void queryForObject_resultNull_exception() throws SQLException {
            // given
            final ResultSet resultSet = mock(ResultSet.class);

            given(statement.executeQuery()).willReturn(resultSet);
            given(resultSet.next()).willReturn(false);
            given(resultSet.getString("account")).willReturn(로마.getAccount());
            given(resultSet.getString("password")).willReturn(로마.getPassword());
            given(resultSet.getString("email")).willReturn(로마.getEmail());

            // when
            final String sql = "select account, password, email from users where account = ?";

            // then
            assertAll(
                    () -> assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, getRowMapper(), "roma"))
                            .isInstanceOf(DataAccessException.class)
                            .hasMessage("query result is null"),
                    () -> verify(statement).executeQuery(),
                    () -> verify(statement).close(),
                    () -> verify(connection).close(),
                    () -> verify(resultSet).close()
            );
        }

    }

    @Test
    @DisplayName("queryForList 메서드는 쿼리를 실행하고 객체의 리스트를 반환한다.")
    void queryForList() throws SQLException {
        // given
        final ResultSet resultSet = mock(ResultSet.class);

        given(statement.executeQuery()).willReturn(resultSet);
        given(resultSet.next()).willReturn(true, true, false);
        given(resultSet.getString("account")).willReturn(로마.getAccount(), 수달.getAccount());
        given(resultSet.getString("password")).willReturn(로마.getPassword(), 수달.getPassword());
        given(resultSet.getString("email")).willReturn(로마.getEmail(), 수달.getEmail());

        // when
        final String sql = "select account, password, email from users";
        final List<User> result = jdbcTemplate.queryForList(sql, getRowMapper());

        // then
        assertAll(
                () -> assertThat(result).contains(로마, 수달),
                () -> verify(statement).executeQuery(),
                () -> verify(statement).close(),
                () -> verify(connection).close(),
                () -> verify(resultSet).close()
        );

    }

    private RowMapper<User> getRowMapper() {
        return rs -> new User(
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email")
        );
    }
}
