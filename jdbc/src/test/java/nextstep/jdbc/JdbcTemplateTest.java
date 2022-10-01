package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement preparedStatement = mock(PreparedStatement.class);
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    private final ResultSet resultSet = mock(ResultSet.class);

    @Test
    void insert() throws SQLException {
        final String sql = "";

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(1L);

        final Long createdId = jdbcTemplate.insert(conn -> conn.prepareStatement(sql));

        assertThat(createdId).isEqualTo(createdId);
    }

    @Test
    void query() throws SQLException {
        final String sql = "";

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        final List<Object> result = jdbcTemplate.query(sql, ((rs, rowNum) -> null), 1004, "대충 더미파라미터");

        assertAll(
                () -> assertThat(result).isEmpty(),
                () -> verify(preparedStatement, times(2)).setObject(anyInt(), any())
        );
    }

    @Test
    void queryForObject() throws SQLException {
        final String sql = "";

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getRow()).thenReturn(1);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.isAfterLast()).thenReturn(true);

        final Object result = jdbcTemplate.queryForObject(sql, ((rs1, rowNum) -> null), 1004, "대충 더미파라미터");

        assertAll(
                () -> assertThat(result).isNull(),
                () -> verify(preparedStatement, times(2)).setObject(anyInt(), any())
        );
    }

    @Test
    @DisplayName("queryForObject의 조회 결과가 없으면 예외를 반환한다.")
    void queryForObjectNoResult() throws SQLException {
        final String sql = "";

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, (rs, rowNum) -> null))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    @DisplayName("queryForObject의 조회 결과가 1보다 많으면 예외가 발생한다.")
    void queryForObjectMultiResult() throws SQLException {
        final String sql = "";

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.isAfterLast()).thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, (rs, rowNum) -> null))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void update() throws SQLException {
        final String sql = "";
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);

        jdbcTemplate.update(connection -> connection.prepareStatement(sql));

        assertAll(
                () -> verify(dataSource).getConnection(),
                () -> verify(connection).prepareStatement(sql),
                () -> verify(preparedStatement).executeUpdate()
        );
    }

    @Test
    @DisplayName("SQLExcetion을 unchecked Exception으로 변경한다.")
    void SQLExceptionToDataAccessException() throws SQLException {
        final String sql = "";
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenThrow(SQLException.class);
        when(preparedStatement.executeQuery()).thenThrow(SQLException.class);

        assertAll(
                () -> assertThatThrownBy(() -> jdbcTemplate.update((conn) -> conn.prepareStatement(sql)))
                        .isInstanceOf(DataAccessException.class),
                () -> assertThatThrownBy(() -> jdbcTemplate.query(sql, (rs, rowNum) -> null))
                        .isInstanceOf(DataAccessException.class),
                () -> assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, (rs, rowNum) -> null))
                        .isInstanceOf(DataAccessException.class)
        );
    }
}
