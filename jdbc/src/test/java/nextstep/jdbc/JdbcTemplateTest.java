package nextstep.jdbc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void update() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);

        jdbcTemplate.update("sql", "arg1", "arg2");

        assertAll(
                () -> verify(preparedStatement, times(2)).setObject(anyInt(), any()),
                () -> verify(preparedStatement, times(1)).executeUpdate(),
                () -> verify(preparedStatement, times(1)).close(),
                () -> verify(connection, times(1)).close()
        );
    }

    @Test
    void query() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        jdbcTemplate.query("sql", rs -> "mockResultSet", "arg1");

        assertAll(
                () -> verify(preparedStatement, times(1)).executeQuery(),
                () -> verify(resultSet, times(1)).next(),
                () -> verify(preparedStatement, times(1)).close(),
                () -> verify(connection, times(1)).close()
        );
    }

    @Test
    void queryForObject() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        jdbcTemplate.queryForObject("sql", rs -> "mockResultSet", "arg2");

        assertAll(
                () -> verify(preparedStatement, times(1)).executeQuery(),
                () -> verify(resultSet, times(1)).next(),
                () -> verify(preparedStatement, times(1)).close(),
                () -> verify(connection, times(1)).close()
        );
    }
}