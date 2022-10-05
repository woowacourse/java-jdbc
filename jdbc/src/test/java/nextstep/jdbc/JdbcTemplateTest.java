package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private PreparedStatement statement;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(statement);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("PreparedStatementSetter를 통해 데이터를 update할 수 있다.")
    @Test
    void update_usingPreparedStatementSetter() {
        //given
        final PreparedStatementSetter preparedStatementSetter = statement -> {
            statement.setString(1, "account");
            statement.setString(2, "password");
            statement.setString(3, "email");
        };

        //when
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, preparedStatementSetter);

        //then
        assertAll(
                () -> verify(statement).setString(1, "account"),
                () -> verify(statement).setString(2, "password"),
                () -> verify(statement).setString(3, "email"),
                () -> verify(statement).executeUpdate()
        );
    }

    @DisplayName("argument를 전달해 데이터를 update할 수 있다.")
    @Test
    void update_passingArguments() {
        //given, when
        final String sql = "insert into users (account, password, email) values (?, ?, ?)";
        jdbcTemplate.update(sql, "account", "password", "email");

        //then
        assertAll(
                () -> verify(statement).setObject(1, "account"),
                () -> verify(statement).setObject(2, "password"),
                () -> verify(statement).setObject(3, "email"),
                () -> verify(statement).executeUpdate()
        );
    }

    @DisplayName("RowMapper를 통해 데이터를 객체 List로 조회할 수 있다.")
    @Test
    void query_RowMapper() throws SQLException {
        //given
        final ResultSet resultSet = mock(ResultSet.class);

        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);
        when(resultSet.getString("account"))
                .thenReturn("forky")
                .thenReturn("boki");
        when(resultSet.getString("password"))
                .thenReturn("password12")
                .thenReturn("password34");
        when(resultSet.getString("email"))
                .thenReturn("forky@email.com")
                .thenReturn("boki@email.com");

        //when
        final String sql = "select id, account, password, email from users";
        final List<String> result = jdbcTemplate.query(sql, rs -> {
            final String account = rs.getString("account");
            final String password = rs.getString("password");
            final String email = rs.getString("email");
            return String.join("/", account, password, email);
        });

        //then
        assertThat(result).containsExactly(
                "forky/password12/forky@email.com", "boki/password34/boki@email.com");
    }

    @DisplayName("RowMapper를 통해 데이터를 객체로 조회할 수 있다.")
    @Test
    void queryForObject_RowMapper() throws SQLException {
        //given
        final ResultSet resultSet = mock(ResultSet.class);

        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("account")).thenReturn("forky");
        when(resultSet.getString("password")).thenReturn("password12");
        when(resultSet.getString("email")).thenReturn("forky@email.com");

        //when
        final String sql = "select id, account, password, email from users where id = ?";
        final String result = jdbcTemplate.queryForObject(sql, rs -> {
            final String account = rs.getString("account");
            final String password = rs.getString("password");
            final String email = rs.getString("email");
            return String.join("/", account, password, email);
        }, 1L);

        //then
        assertThat(result).isEqualTo("forky/password12/forky@email.com");
    }
}
