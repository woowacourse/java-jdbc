package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import nextstep.jdbc.element.RowMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {
    private final DataSource dataSource = mock(DataSource.class);
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @DisplayName("queryForObject 호출 시 결과 반환 후 connection, statement, resultSet이 close된다.")
    @Test
    void queryForObject() throws SQLException {
        //given
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);

        //when
        final var select = "select id, account, password, email from users where id = ?";
        jdbcTemplate.queryForObject(select, mock(RowMapper.class), 1L);

        //then
        verify(statement).close();
        verify(resultSet).close();
        verify(connection).close();
    }

    @DisplayName("query 호출 시 결과 반환 후 connection, statement, resultSet이 close된다.")
    @Test
    void query() throws SQLException {
        //given
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);

        //when
        final var select = "select id, account, password, email from users where account = ?";
        jdbcTemplate.query(select, mock(RowMapper.class), "hunch");

        //then
        verify(statement).close();
        verify(resultSet).close();
        verify(connection).close();
    }

    @DisplayName("executeUpdate 호출 시 결과 반환 후 connection, statement이 close된다.")
    @Test
    void executeUpdate() throws SQLException {
        //given
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(5);

        //when
        final var select = "update users set account = ?";
        Integer integer = jdbcTemplate.executeUpdate(select, mock(RowMapper.class), "hunch");

        //then
        assertThat(integer).isEqualTo(5);
        verify(statement).close();
        verify(connection).close();
    }
}
