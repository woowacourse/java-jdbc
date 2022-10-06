package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;
    private final static RowMapper<Object> ROW_MAPPER = rs -> new Object();

    @BeforeEach
    void setUp() {
        dataSource = mock(DataSource.class);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void update() throws SQLException {
        var connection = mock(Connection.class);
        var statement = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(statement);

        jdbcTemplate.update("sql");

        verify(dataSource, times(1)).getConnection();
        verify(connection, times(1)).prepareStatement(any());
    }

    @Test
    void queryForObjectWhenNextTrue() throws SQLException {
        var connection = mock(Connection.class);
        var statement = mock(PreparedStatement.class);
        var resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        Object result = jdbcTemplate.queryForObject("sql", ROW_MAPPER);

        verify(dataSource, times(1)).getConnection();
        verify(connection, times(1)).prepareStatement(any());
        verify(statement, times(1)).executeQuery();
        verify(resultSet, times(1)).next();
        assertThat(result).isInstanceOf(Object.class);
    }

    @Test
    void queryForObjectWhenNextFalse() throws SQLException {
        var connection = mock(Connection.class);
        var statement = mock(PreparedStatement.class);
        var resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject("sql", ROW_MAPPER))
                .isExactlyInstanceOf(IllegalStateException.class);
    }

    @Test
    void queryWhenNextTrue() throws SQLException {
        var connection = mock(Connection.class);
        var statement = mock(PreparedStatement.class);
        var resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        List<Object> result = jdbcTemplate.query("sql", ROW_MAPPER);

        verify(dataSource, times(1)).getConnection();
        verify(connection, times(1)).prepareStatement(any());
        verify(statement, times(1)).executeQuery();
        verify(resultSet, times(1)).next();
        assertThat(result).hasSize(1);
    }

    @Test
    void queryWhenNextFalse() throws SQLException {
        var connection = mock(Connection.class);
        var statement = mock(PreparedStatement.class);
        var resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        List<Object> result = jdbcTemplate.query("sql", ROW_MAPPER);

        verify(dataSource, times(1)).getConnection();
        verify(connection, times(1)).prepareStatement(any());
        verify(statement, times(1)).executeQuery();
        verify(resultSet, times(1)).next();
        assertThat(result).hasSize(0);
    }
}
