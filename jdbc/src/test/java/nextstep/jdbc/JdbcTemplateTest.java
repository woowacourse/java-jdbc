package nextstep.jdbc;

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
    private DataSourceTransactionManager transactionManager;
    private TransactionStatus status;
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
        transactionManager = new DataSourceTransactionManager(dataSource);
        status = transactionManager.getTransaction(new DefaultTransactionDefinition());

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
            given(resultSet.getString("account")).willReturn("roma");
            given(resultSet.getString("password")).willReturn("1234");
            given(resultSet.getString("email")).willReturn("roma@service.apply");

            // when
            final String sql = "select account, password, email from users where account = ?";
            final String result = jdbcTemplate.queryForObject(sql, getRowMapper(), "roma");

            // then
            assertAll(
                    () -> assertThat(result).isEqualTo("roma/1234/roma@service.apply"),
                    () -> verify(statement).setObject(1, "roma"),
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
            given(resultSet.getString("account")).willReturn("roma");
            given(resultSet.getString("password")).willReturn("1234");
            given(resultSet.getString("email")).willReturn("roma@service.apply");

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
            given(resultSet.getString("account")).willReturn("roma");
            given(resultSet.getString("password")).willReturn("1234");
            given(resultSet.getString("email")).willReturn("roma@service.apply");

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
        given(resultSet.getString("account")).willReturn("roma", "jason");
        given(resultSet.getString("password")).willReturn("1234", "4321");
        given(resultSet.getString("email")).willReturn("roma@service.apply", "jason@service.apply");

        // when
        final String sql = "select account, password, email from users";
        final List<String> result = jdbcTemplate.queryForList(sql, getRowMapper());

        // then
        assertAll(
                () -> assertThat(result).contains("roma/1234/roma@service.apply", "jason/4321/jason@service.apply"),
                () -> verify(statement).executeQuery(),
                () -> verify(statement).close(),
                () -> verify(connection).close(),
                () -> verify(resultSet).close()
        );

    }

    private RowMapper<String> getRowMapper() {
        return rs -> String.format("%s/%s/%s",
                rs.getString("account"),
                rs.getString("password"),
                rs.getString("email")
        );
    }
}
