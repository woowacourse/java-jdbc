package nextstep.jdbc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapper;

class JdbcTemplateTest {

    private DataSource dataSource;

    @BeforeEach
    void setUp() {
        this.dataSource = mock(DataSource.class);
    }

    @Test
    void SELECT를_실행했을_때_하나의_객체_결과_값을_도출할_수_있다() throws SQLException {
        // given
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String sql = "SELECT * from users";

        // when
        jdbcTemplate.queryForObject(sql, mock(RowMapper.class));

        // then
        verify(dataSource).getConnection();
        verify(connection).prepareStatement(anyString());
        verify(connection).close();
        verify(statement).close();
    }
}
