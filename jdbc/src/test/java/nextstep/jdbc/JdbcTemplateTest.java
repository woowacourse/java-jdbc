package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private static final RowMapper<String> testRowMapper = resultSet -> "entity";

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement preparedStatement = mock(PreparedStatement.class);
    private final ResultSet resultSet = mock(ResultSet.class);
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);

    @BeforeEach
    private void setUp() throws SQLException {
        when(dataSource.getConnection())
                .thenReturn(connection);
        when(connection.prepareStatement(any()))
                .thenReturn(preparedStatement);
        when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
        when(resultSet.next())
                .thenReturn(true)
                .thenReturn(false);
    }

    @Test
    void update() throws SQLException {
        // when
        jdbcTemplate.update("sql", "name");

        // then
        verify(dataSource).getConnection();
        verify(connection).prepareStatement("sql");
        verify(preparedStatement).setObject(1, "name");
    }

    @Test
    void queryForObject() throws SQLException {
        // when
        Optional<String> target = jdbcTemplate.queryForObject("sql", testRowMapper, "name");

        // then
        verify(dataSource).getConnection();
        verify(connection).prepareStatement("sql");
        verify(preparedStatement).setObject(1, "name");
        assertThat(target.get()).isEqualTo("entity");
    }

    @Test
    void query() throws SQLException {
        // when
        List<String> result = jdbcTemplate.query("sql", testRowMapper);

        // then
        verify(dataSource).getConnection();
        verify(connection).prepareStatement("sql");
        assertThat(result).hasSize(1);
    }
}
