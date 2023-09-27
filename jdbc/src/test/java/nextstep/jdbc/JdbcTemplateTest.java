package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    @Mock
    private DataSource dataSource;
    @Mock
    private Connection connection;
    @Mock
    private PreparedStatement preparedStatement;
    @Mock
    private ResultSet resultSet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void update() throws SQLException {
        // given
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        String testSql = "UPDATE some_table SET column1 = ?, column2 = ? WHERE id = ?";
        Object[] params = {"value1", "value2", 1};

        // when
        jdbcTemplate.update(testSql, params);

        // then
        verify(preparedStatement, times(1)).setObject(1, "value1");
        verify(preparedStatement, times(1)).setObject(2, "value2");
        verify(preparedStatement, times(1)).setObject(3, 1);
        verify(preparedStatement, times(1)).executeUpdate();
    }

    @Test
    void queryForObject() throws SQLException {
        // given
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        RowMapper<String> rowMapper = (rs, rowNum) -> rs.getString("column_name");
        when(resultSet.getString("column_name")).thenReturn("mockValue");

        // when
        String sql = "SELECT * FROM some_table where id = ? ";
        Optional<String> result = jdbcTemplate.queryForObject(sql, rowMapper, 1);

        // then
        assertThat(result).isPresent();
        assertThat(result.get()).contains("mockValue");
    }

    @Test
    void query() throws SQLException {
        // given
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        when(resultSet.getString("column_name")).thenReturn("value1", "value2");

        RowMapper<String> rowMapper = (rs, rowNum) -> rs.getString("column_name");

        // when
        String sql = "SELECT * FROM some_table ";
        List<String> results = jdbcTemplate.query(sql, rowMapper);

        // then
        assertThat(results).hasSize(2)
                .contains("value1", "value2");
    }

}
