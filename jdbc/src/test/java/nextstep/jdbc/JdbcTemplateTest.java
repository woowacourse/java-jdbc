package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class JdbcTemplateTest {

    private static final RowMapper<String> testRowMapper = resultSet -> "entity";

    private final DataSource dataSource = Mockito.mock(DataSource.class);
    private final Connection connection = Mockito.mock(Connection.class);
    private final PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);
    private final ResultSet resultSet = Mockito.mock(ResultSet.class);

    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(this.dataSource);

    @BeforeEach
    private void setUp() throws SQLException {
        Mockito.when(dataSource.getConnection())
                .thenReturn(connection);
        Mockito.when(connection.prepareStatement(Mockito.any()))
                .thenReturn(preparedStatement);
        Mockito.when(preparedStatement.executeQuery())
                .thenReturn(resultSet);
        Mockito.when(resultSet.next())
                .thenReturn(false);
    }

    @Test
    void update() throws SQLException {
        // when
        jdbcTemplate.update("sql", "name");

        // then
        Mockito.verify(dataSource).getConnection();
        Mockito.verify(connection).prepareStatement("sql");
        Mockito.verify(preparedStatement).setObject(1, "name");
    }

    @Test
    void queryForObject() throws SQLException {
        // when
        Object target = jdbcTemplate.queryForObject("sql", testRowMapper, "name");

        // then
        Mockito.verify(dataSource).getConnection();
        Mockito.verify(connection).prepareStatement("sql");
        Mockito.verify(preparedStatement).setObject(1, "name");
        Mockito.verify(resultSet).next();
        assertThat(target).isEqualTo("entity");
    }

    @Test
    void query() throws SQLException {
        // when
        List<String> result = jdbcTemplate.query("sql", testRowMapper);

        // then
        Mockito.verify(dataSource).getConnection();
        Mockito.verify(connection).prepareStatement("sql");
        Mockito.verify(resultSet).next();
        assertThat(result).hasSize(0);
    }
}
