package nextstep.jdbc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JdbcTemplateTest {

    private Connection connection;
    private PreparedStatement preparedStatement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        DataSource dataSource = mock(DataSource.class);
        jdbcTemplate = new JdbcTemplate(dataSource);

        given(dataSource.getConnection()).willReturn(connection);
        given(connection.prepareStatement(anyString())).willReturn(preparedStatement);
        given(preparedStatement.executeQuery()).willReturn(resultSet);
    }

    @DisplayName("query() 메서드 실행 후에 관련 자원이 close 되는지 확인한다.")
    @Test
    void query_WhetherResourceClosed() throws SQLException {
        // when
        jdbcTemplate.query("select * from users", rs -> null);

        // then
        verify(connection).close();
        verify(preparedStatement).close();
        verify(resultSet).close();
    }

    @DisplayName("queryForObject() 메서드 실행 후에 관련 자원이 close 되는지 확인한다.")
    @Test
    void queryForObject_WhetherResourceClosed() throws SQLException {
        // when
        jdbcTemplate.queryForObject("select * from users", rs -> null);

        // then
        verify(connection).close();
        verify(preparedStatement).close();
        verify(resultSet).close();
    }

    @DisplayName("update() 메서드 실행 후에 관련 자원이 close 되는지 확인한다.")
    @Test
    void update_WhetherResourceClosed() throws SQLException {
        // when
        jdbcTemplate.update("insert into users (account, password, email) values (?, ?, ?)");

        // then
        verify(connection).close();
        verify(preparedStatement).close();
    }
}
