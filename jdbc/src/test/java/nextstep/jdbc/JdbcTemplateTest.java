package nextstep.jdbc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private DataSource dataSourceMock;
    private Connection connectionMock;
    private PreparedStatement preparedStatementMock;

    @BeforeEach
    void setUp() throws SQLException {
        dataSourceMock = mock(DataSource.class);
        connectionMock = mock(Connection.class);
        preparedStatementMock = mock(PreparedStatement.class);

        given(dataSourceMock.getConnection()).willReturn(connectionMock);
        given(connectionMock.prepareStatement(anyString())).willReturn(preparedStatementMock);

        jdbcTemplate = new JdbcTemplate(dataSourceMock);
    }

    @Test
    void updateTest() throws SQLException {
        //given
        String sql = "insert into users (account, password, email) values (?, ?, ?)";
        String account = "account";
        String password = "pwd";
        String email = "email";

        //when
        int result = jdbcTemplate.update(sql, account, password, email);

        //then
        then(preparedStatementMock).should(times(3)).setObject(anyInt(), any());
        then(preparedStatementMock).should(times(1)).executeUpdate();
    }
}
