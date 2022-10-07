package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
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

class JdbcTemplateTest {

    private RowMapper<TestUser> rowMapper() {
        return (rs, rowNum) -> new TestUser(1L);
    }

    @Test
    @DisplayName("여러개의 결과가 나오는 query")
    void query() throws SQLException {
        // given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);

        final String sql = "select id from user";

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // when
        final List<TestUser> users = jdbcTemplate.query(sql, rowMapper());

        // then
        assertThat(users).hasSize(2);
    }

    @Test
    @DisplayName("query 메서드 이후에 resource가 잘 닫히는지 확인")
    void queryCloseResource() throws SQLException {
        // given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);

        final String sql = "select id from user";

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // when
        jdbcTemplate.query(sql, rowMapper());

        // then
        assertAll(
                () -> verify(connection).close(),
                () -> verify(preparedStatement).close(),
                () -> verify(resultSet).close()
        );
    }

    @Test
    @DisplayName("1개의 결과가 나오는 queryForObject")
    void queryForObject() throws SQLException {
        // given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);

        final String sql = "select id from user where id = ?";

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // when
        final TestUser testUser = jdbcTemplate.queryForObject(sql, rowMapper(), 1L);

        // then
        assertThat(testUser).isNotNull();
    }

    @Test
    @DisplayName("2개의 결과가 나오는 queryForObject는 예외가 발생하고 Checked Exception이 아닌 Unchecked Exception이 반환되도록 한다")
    void queryForObjectException() throws SQLException {
        // given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);

        final String sql = "select id from user where account = ?";

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getRow()).thenReturn(2);

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // when, then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper(), "tiki"))
                .isExactlyInstanceOf(DataAccessException.class);
    }

    @Test
    @DisplayName("queryForObject 메서드 이후에 resource가 잘 닫히는지 확인")
    void queryForObjectCloseResource() throws SQLException {
        // given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);

        final String sql = "select id from user where id = ?";

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // when
        jdbcTemplate.queryForObject(sql, rowMapper(), 1L);

        // then
        assertAll(
                () -> verify(connection).close(),
                () -> verify(preparedStatement).close(),
                () -> verify(resultSet).close()
        );
    }

    @Test
    @DisplayName("update 로직 테스트")
    void update() throws SQLException {
        // given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        final String sql = "insert into users (id) values (?)";

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // when
        final int update = jdbcTemplate.update(sql, rowMapper(), 1L);

        // then
        assertThat(update).isEqualTo(1);
    }

    @Test
    @DisplayName("update 메서드 이후에 resource가 잘 닫히는지 확인")
    void updateCloseResource() throws SQLException {
        // given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);

        final String sql = "insert into users (id) values (?)";

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // when
        jdbcTemplate.update(sql, rowMapper(), 1L);

        // then
        assertAll(
                () -> verify(connection).close(),
                () -> verify(preparedStatement).close()
        );
    }

    @Test
    @DisplayName("sql이 잘못되었을 때 Checked Exception이 아닌 Unchecked Exception이 반환되도록 한다")
    void invalidSqlThrowUncheckedException() throws SQLException {
        // given
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);

        final String sql = "invalid";

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenThrow(SQLException.class);

        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        // when, then
        assertThatThrownBy(() -> jdbcTemplate.update(sql, rowMapper(), 1L))
                .isExactlyInstanceOf(DataAccessException.class);
    }
}
