package nextstep.jdbc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    @BeforeEach
    void setUp() throws Exception {
        this.connection = mock(Connection.class);
        this.preparedStatement = mock(PreparedStatement.class);
        this.resultSet = mock(ResultSet.class);

        DataSource dataSource = mock(DataSource.class);
        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(anyString())).willReturn(preparedStatement);
        given(connection.getAutoCommit()).willReturn(true);
        given(preparedStatement.executeQuery()).willReturn(resultSet);
        given(preparedStatement.executeQuery(anyString())).willReturn(resultSet);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void closeWhenQuery() throws SQLException {
        jdbcTemplate.query("SELECT name FROM MEMBER", (rs, rownum) -> 1);

        verify(preparedStatement).executeQuery();
        verify(connection).close();
        verify(preparedStatement).close();
        verify(resultSet).close();
    }

    @Test
    void closeWhenUpdate() throws SQLException {
        jdbcTemplate.update("insert into users (account, password, email) values (?, ?, ?)", "huni", "12",
                "e@email.com");

        verify(preparedStatement).executeUpdate();
        verify(connection).close();
        verify(preparedStatement).close();
    }
}
