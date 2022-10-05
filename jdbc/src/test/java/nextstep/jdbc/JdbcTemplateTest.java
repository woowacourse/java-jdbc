package nextstep.jdbc;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapper;

class JdbcTemplateTest {

    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        this.dataSource = mock(DataSource.class);
    }

    @Test
    @DisplayName("자원을 종료한다.")
    void closeResources() throws SQLException {
        // given
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getString("account")).thenReturn("test");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "SELECT * from users where id = 1";

        // when
        jdbcTemplate.queryForObject(sql, mock(RowMapper.class));

        // then
        assertAll(
                () -> verify(dataSource).getConnection(),
                () -> verify(connection).prepareStatement(anyString()),
                () -> verify(connection).close(),
                () -> verify(statement).close()
        );
    }
}
