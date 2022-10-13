package nextstep.jdbc;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

    @DisplayName("queryForObject의 결과가 0개이면 예외가 발생한다.")
    @Test
    void queryForObject_exception_emptyResult() throws SQLException {
        final String sql = "결과가 0개인 쿼리";
        final RowMapper rowMapper = mock(RowMapper.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(false);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    @DisplayName("queryForObject의 결과가 2개 이상이면 예외가 발생한다.")
    @Test
    void queryForObject_exception_incorrectResultSize() throws SQLException {
        final String sql = "결과가 2개인 쿼리";
        final RowMapper rowMapper = mock(RowMapper.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, true, false);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }

    @DisplayName("queryForObject의 결과가 1개이면 정상적으로 결과값을 반환한다.")
    @Test
    void queryForObject_exception_correctResultSize() throws SQLException {
        final String sql = "결과가 1개인 쿼리";
        final RowMapper rowMapper = mock(RowMapper.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, false);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);

        assertDoesNotThrow(() -> jdbcTemplate.queryForObject(sql, rowMapper));
    }
}
