package nextstep.jdbc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.any;

class JdbcTemplateTest {

    @DisplayName("사용 후 커넥션을 자동 반환한다")
    @Test
    public void closeConnectionTest() throws SQLException {
        DataSource dataSource = Mockito.mock(DataSource.class);
        Connection conn = Mockito.mock(Connection.class);
        PreparedStatement preparedStatement = Mockito.mock(PreparedStatement.class);

        Mockito.when(dataSource.getConnection()).thenReturn(conn);
        Mockito.when(conn.prepareStatement(any())).thenReturn(preparedStatement);

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.update("mockQuery");

        Mockito.verify(conn).close();
        Mockito.verify(preparedStatement).close();
    }
}
