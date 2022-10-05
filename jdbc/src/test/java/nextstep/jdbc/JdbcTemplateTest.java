package nextstep.jdbc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
        given(connection.prepareStatement(any())).willReturn(preparedStatement);
        given(preparedStatement.executeQuery()).willReturn(resultSet);
    }

    @DisplayName(value = "query 호출 시 모든 자원을 close 했는지 확인")
    @Test
    void query() throws SQLException {
        // given
        String sql = "select * from test";

        // when
        jdbcTemplate.query(sql, null);

        // then
        verify(connection).close();
        verify(preparedStatement).close();
        verify(resultSet).close();
    }

    @DisplayName(value = "update 호출 시 모든 자원을 close 했는지 확인")
    @Test
    void update() throws SQLException {
        // given
        String sql = "insert into test values ('test')";

        // when
        jdbcTemplate.update(sql);

        // then
        verify(connection).close();
        verify(preparedStatement).close();
    }
}
