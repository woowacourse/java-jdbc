package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import nextstep.jdbc.exception.IllegalDataSizeException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

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

        when(resultSet.next())
                .thenReturn(true)
                .thenReturn(false);

        when(resultSet.getString("username"))
                .thenReturn("dwoo");

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final String sql = "select username from users where id = ?";

        final List<String> result = jdbcTemplate.query(sql, getMockRowMapper(), 1L);
        assertThat(result).hasSize(1)
                .containsExactly("dwoo");

        verify(dataSource).getConnection();
        verify(connection).prepareStatement(sql);
        verify(preparedStatement).executeQuery();
        verify(preparedStatement).close();
        verify(connection).close();
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

        when(resultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        when(resultSet.getString("username"))
                .thenReturn("dwoo")
                .thenReturn("jung");

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        final String sql = "select username from users";

        final List<String> result = jdbcTemplate.query(sql, getMockRowMapper());
        assertThat(result).hasSize(2)
                .containsExactly("dwoo", "jung");

        verify(dataSource).getConnection();
        verify(connection).prepareStatement(sql);
        verify(preparedStatement).executeQuery();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @DisplayName("queryForObject 메소드를 호출하여 한 건의 조회를 반환할 수 있다.")
    @Test
    void queryForObject() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next())
                .thenReturn(true)
                .thenReturn(false);

        when(resultSet.getString("username"))
                .thenReturn("dwoo");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        final String sql = "select username from users where id = ?";
        final String result = jdbcTemplate.queryForObject(sql, getMockRowMapper(), 1L);

        assertThat(result).isEqualTo("dwoo");
        verify(dataSource).getConnection();
        verify(connection).prepareStatement(sql);
        verify(preparedStatement).executeQuery();
        verify(preparedStatement).close();
        verify(connection).close();
    }

    @DisplayName("queryForObject 메소드를 호출하여 조회한 결과가 한 건 이상인 경우 예외가 발생한다.")
    @Test
    void queryForObjectWithMultipleData() throws SQLException {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(any())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        when(resultSet.getString("username"))
                .thenReturn("dwoo")
                .thenReturn("jung");

        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        final String sql = "select username from users where id = ?";

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, getMockRowMapper(), 1L))
                .isInstanceOf(IllegalDataSizeException.class);

        verify(dataSource).getConnection();
        verify(connection).prepareStatement(sql);
        verify(preparedStatement).executeQuery();
        verify(preparedStatement).close();
        verify(connection).close();
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
        final var sql = "insert into users (username, password, email) values (?, ?, ?)";
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

    private static RowMapper<String> getMockRowMapper() {
        return (resultSet) -> resultSet.getString("username");
    }
}
