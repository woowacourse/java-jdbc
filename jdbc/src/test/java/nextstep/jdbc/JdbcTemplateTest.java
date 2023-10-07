package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private Connection connection;

    @BeforeEach
    void setUp() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(mock(Statement.class));

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("update 를 수행한다.")
    @Test
    void update() throws SQLException {
        // given
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final Statement statement = mock(Statement.class);
        when(connection.createStatement())
            .thenReturn(statement);
        when(statement.getConnection())
            .thenReturn(connection);
        when(connection.prepareStatement(anyString()))
            .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate())
            .thenReturn(1);

        final String sql = "insert into users (account, password, email) values (?, ?, ?)";

        // when
        jdbcTemplate.update(sql, "vero", "password", "email");

        // then
        assertAll(
            () -> verify(connection, times(1)).prepareStatement(sql),
            () -> verify(preparedStatement, times(1)).setObject(1, "vero"),
            () -> verify(preparedStatement, times(1)).setObject(2, "password"),
            () -> verify(preparedStatement, times(1)).setObject(3, "email"),
            () -> verify(preparedStatement, times(1)).executeUpdate()
        );
    }

    @DisplayName("RowMapper 를 입력 받아 query 를 수행한다.")
    @Test
    void queryRowMapper() throws SQLException {
        // given
        final String sql = "select account, password, email from users where id = ?";
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final Statement statement = mock(Statement.class);
        when(connection.createStatement())
            .thenReturn(statement);
        when(statement.getConnection())
            .thenReturn(connection);
        when(connection.prepareStatement(anyString()))
            .thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate())
            .thenReturn(1);
        final ResultSet mockResultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery())
            .thenReturn(mockResultSet);
        when(mockResultSet.getString("account")).thenReturn("vero");
        when(mockResultSet.getString("password")).thenReturn("password");
        when(mockResultSet.getString("email")).thenReturn("email");

        // when
        final Map<String, String> result = jdbcTemplate.query(sql, new RowMapper<>() {
            @Nonnull
            @Override
            public Map<String, String> mapRow(final ResultSet resultSet) throws SQLException {
                return Map.of(
                    "account", resultSet.getString("account"),
                    "password", resultSet.getString("password"),
                    "email", resultSet.getString("email")
                );
            }
        }, 1L);

        // then
        assertAll(
            () -> verify(connection, times(1)).prepareStatement(sql),
            () -> verify(preparedStatement, times(1)).setObject(1, 1L),
            () -> verify(preparedStatement, times(1)).executeQuery(),
            () -> verify(mockResultSet, times(1)).getString("account"),
            () -> verify(mockResultSet, times(1)).getString("password"),
            () -> verify(mockResultSet, times(1)).getString("email"),
            () -> assertThat(result).containsEntry("account", "vero"),
            () -> assertThat(result).containsEntry("email", "email"),
            () -> assertThat(result).containsEntry("password", "password")
        );
    }

    @DisplayName("sql 을 입력받아 execute 를 수행한다.")
    @Test
    void executeSql() throws SQLException {
        // given
        final String sql = "select count(*) from users";
        final Statement mockStatement = mock(Statement.class);
        when(connection.createStatement())
            .thenReturn(mockStatement);
        when(mockStatement.execute(sql))
            .thenReturn(true);

        // when
        jdbcTemplate.execute(sql);

        // then
        assertAll(
            () -> verify(connection, times(1)).createStatement(),
            () -> verify(mockStatement, times(1)).execute(sql)
        );
    }

    @DisplayName("Class 타입을 입력 받아 쿼리 후, List 를 반환한다.")
    @Test
    void queryForList() throws SQLException {
        // given
        final String sql = "select email, password from users";
        final Statement mockStatement = mock(Statement.class);
        when(connection.createStatement())
            .thenReturn(mockStatement);
        final ResultSet mockResultSet = mock(ResultSet.class);
        when(mockStatement.executeQuery(sql))
            .thenReturn(mockResultSet);
        when(mockResultSet.next())
            .thenReturn(true)
            .thenReturn(true)
            .thenReturn(false);
        when(mockResultSet.getObject("email", String.class))
            .thenReturn("email1")
            .thenReturn("email2");
        when(mockResultSet.getObject("password", String.class))
            .thenReturn("password1")
            .thenReturn("password2");

        // when
        final List<User> users = jdbcTemplate.queryForList(sql, new RowMapper<>() {
            @Nonnull
            @Override
            public User mapRow(final ResultSet resultSet) throws SQLException {
                return new User(
                    resultSet.getObject("email", String.class),
                    resultSet.getObject("password", String.class)
                );
            }
        });

        // then
        assertAll(
            () -> verify(connection, times(1)).createStatement(),
            () -> verify(mockStatement, times(1)).executeQuery(sql),
            () -> verify(mockResultSet, times(3)).next(),
            () -> verify(mockResultSet, times(2)).getObject("email", String.class),
            () -> verify(mockResultSet, times(2)).getObject("password", String.class),
            () -> assertThat(users).hasSize(2),
            () -> assertThat(users.get(0).email).isEqualTo("email1"),
            () -> assertThat(users.get(0).password).isEqualTo("password1"),
            () -> assertThat(users.get(1).email).isEqualTo("email2"),
            () -> assertThat(users.get(1).password).isEqualTo("password2")
        );
    }

    static class User {

        private String email;
        private String password;

        public User() {
        }

        public User(final String email, final String password) {
            this.email = email;
            this.password = password;
        }
    }
}
