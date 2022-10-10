package nextstep.jdbc;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {


    private final DataSource dataSource = mock(DataSource.class);
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    private final Connection connection = mock(Connection.class);

    @Test
    @DisplayName("queryForObject의 결과가 1개일때 올바르게 작동한다.")
    void queryForObject_success() throws SQLException {
        // given
        final String sql = "aki_is_gosu";
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final RowMapper rowMapper = mock(RowMapper.class);
        final ResultSet resultSet = mock(ResultSet.class);

        // when
        when(dataSource.getConnection()).thenReturn(connection);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(resultSet.next()).thenReturn(true, false);

        // then
        assertDoesNotThrow(() -> jdbcTemplate.queryForObject(sql, rowMapper));
    }

    @Test
    @DisplayName("queryForObject의 결과가 0개일때 에러가 발생한다.")
    void queryForObject_fail_zero() throws SQLException {
        // given
        final String sql = "aki_very_gosu";
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final RowMapper rowMapper = mock(RowMapper.class);
        final ResultSet resultSet = mock(ResultSet.class);

        // when
        when(dataSource.getConnection()).thenReturn(connection);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(resultSet.next()).thenReturn(false);

        // then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    @DisplayName("queryForObject의 결과가 2개일때 에러가 발생한다.")
    void queryForObject_fail_two() throws SQLException {
        // given
        final String sql = "aki_go_baemin";
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final RowMapper rowMapper = mock(RowMapper.class);
        final ResultSet resultSet = mock(ResultSet.class);

        // when
        when(dataSource.getConnection()).thenReturn(connection);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);
        when(resultSet.next()).thenReturn(true, true, false);

        // then
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper))
                .isInstanceOf(DataAccessException.class);
    }
}
