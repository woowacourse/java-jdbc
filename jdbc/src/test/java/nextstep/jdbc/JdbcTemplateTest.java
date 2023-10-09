package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        jdbcTemplate = new JdbcTemplate(dataSource);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(dataSource.getConnection()).thenReturn(connection);
    }

    @Test
    void testQuerySuccess() {
        //given
        final String sql = "SELECT id FROM table WHERE name = ?";
        final String argument = "success";

        //when
        final String result = jdbcTemplate.query(sql, preparedStatement -> argument, argument);

        //then
        assertThat(result).isEqualTo(argument);
    }

    @Test
    void testQueryForList() throws SQLException {
        //given
        final String sql = "SELECT id FROM table WHERE name = ?";
        final ResultSet resultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        //when
        final List<String> results = jdbcTemplate.queryForList(sql, resultSet1 -> "result");

        //then
        assertThat(results).usingRecursiveComparison()
                .isEqualTo(List.of("result", "result"));
    }

    @Test
    void testQueryForObject() throws SQLException {
        //given
        final String sql = "SELECT id FROM table WHERE name = ?";
        final ResultSet resultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true)
                .thenReturn(false);

        //when
        final Optional<String> result = jdbcTemplate.queryForObject(sql, rowMapper -> "result");

        //then
        assertAll(
                () -> assertThat(result).isPresent(),
                () -> assertThat(result).contains("result")
        );
    }

    @Test
    void testUpdate() {
        // given
        final String sql = "UPDATE table SET name = ? WHERE id = ?";

        // when
        jdbcTemplate.update(sql, "John", 1);

        // then
        assertAll(
                () -> verify(preparedStatement, times(1)).executeUpdate(),
                () -> verify(preparedStatement, times(1)).setObject(1, "John"),
                () -> verify(preparedStatement, times(1)).setObject(2, 1)
        );
    }
}
