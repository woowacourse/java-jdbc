package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.RowMapper;

class JdbcTemplateTest {

    @DisplayName("JdbcTemplate 이 연결된 connection을 반환해줄 수 있다.")
    @Test
    void getConnection() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);

        when(dataSource.getConnection()).thenReturn(connection);

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final Connection gottenConnection = jdbcTemplate.getConnection();

        assertThat(gottenConnection).isEqualTo(connection);
        verify(dataSource).getConnection();
    }

    @DisplayName("인자와 함께 query 메소드를 호출하여 예외없이 조회 결과를 반환한다.")
    @Test
    void queryWithArgs() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final String sql = "select id, account, password, email from users where account = ?";

        assertDoesNotThrow(() -> jdbcTemplate.query(sql, getMockRowMapper(), "dwoo"));
        verify(dataSource).getConnection();
        verify(connection).prepareStatement(sql);
        verify(preparedStatement).executeQuery();
    }

    @DisplayName("인자없이 query 메소드를 호출하여 예외없이 조회 결과를 반환한다.")
    @Test
    void queryWithNonArgs() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final String sql = "select id, account, password, email from users";

        assertDoesNotThrow(() -> jdbcTemplate.query(sql, getMockRowMapper()));
        verify(dataSource).getConnection();
        verify(connection).prepareStatement(sql);
        verify(preparedStatement).executeQuery();
    }

    private static RowMapper<Object> getMockRowMapper() {
        return (resultSet, rowNumber) -> any();
    }
}
