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
import java.util.List;
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

    @DisplayName("queryForObject 메소드를 호출하여 한 건의 조회를 반환할 수 있다.")
    @Test
    void queryForObject() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(any(), any(), any())).thenReturn(List.of("result"));

        final String sql = "select result from table where account = ?";
        final Object result = jdbcTemplate.queryForObject(sql, getMockRowMapper(), "dwoo");

        assertThat(result.toString()).isEqualTo("[result]");
        verify(jdbcTemplate).queryForObject(any(), any(), any());
    }

    @DisplayName("updat 메소드를 호출하여 데이터를 저장할 수 있고, 저장된 row 수를 반환한다.")
    @Test
    void insert() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final var sql = "insert into users (account, password, email) values (?, ?, ?)";
        final int updatedRowCount = jdbcTemplate.update(sql, "dwoo", "password", "email@email.com");

        assertThat(updatedRowCount).isEqualTo(1);
        verify(preparedStatement).executeUpdate();
    }

    @DisplayName("update 메소드를 호출하여 데이터를 변경에 성공하면 변경된 row수를 반환한다.")
    @Test
    void update() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final var sql = "update users set account = ?, password = ?, email = ? WHERE id = ?";
        final int updatedRowCount = jdbcTemplate.update(sql, 1L);

        assertThat(updatedRowCount).isEqualTo(1);
        verify(preparedStatement).executeUpdate();
    }

    private static RowMapper<Object> getMockRowMapper() {
        return (resultSet, rowNumber) -> any();
    }
}
