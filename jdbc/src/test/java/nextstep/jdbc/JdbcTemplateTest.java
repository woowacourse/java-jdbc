package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.sql.DataSource;
import nextstep.jdbc.exception.EmptyResultDataAccessException;
import nextstep.jdbc.exception.IncorrectResultSizeDataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class JdbcTemplateTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    private final Connection connection = mock(Connection.class);

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
    }

    @DisplayName("sql문이 null이면 예외가 발생한다.")
    @Test
    void sql_exception_nullQuery() {
        final String sql = null;

        assertAll(
                () -> assertThatThrownBy(() -> jdbcTemplate.update(sql))
                        .isInstanceOf(IllegalArgumentException.class),
                () -> assertThatThrownBy(() -> jdbcTemplate.query(sql, mock(RowMapper.class)))
                        .isInstanceOf(IllegalArgumentException.class),
                () -> assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, mock(RowMapper.class)))
                        .isInstanceOf(IllegalArgumentException.class)
        );
    }

    @DisplayName("sql문이 빈칸이면 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(strings = {"", " ", "   "})
    void sql_exception_blankQuery(final String sql) {
        assertAll(
                () -> assertThatThrownBy(() -> jdbcTemplate.update(sql))
                        .isInstanceOf(IllegalArgumentException.class),
                () -> assertThatThrownBy(() -> jdbcTemplate.query(sql, mock(RowMapper.class)))
                        .isInstanceOf(IllegalArgumentException.class),
                () -> assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, mock(RowMapper.class)))
                        .isInstanceOf(IllegalArgumentException.class)
        );
    }

    @DisplayName("queryForObject의 결과가 0개이면 예외가 발생한다.")
    @Test
    void queryForObject_exception_EmptyResult() throws SQLException {
        final String sql = "결과가 0개인 쿼리";
        final RowMapper rowMapper = mock(RowMapper.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(false);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }

    @DisplayName("queryForObject의 결과가 1개보다 많으면 예외가 발생한다.")
    @Test
    void queryForObject_exception_IncorrectResultSize() throws SQLException {
        final String sql = "결과가 3개인 쿼리";
        final RowMapper rowMapper = mock(RowMapper.class);
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);

        when(resultSet.next()).thenReturn(true, true, true, false);
        when(connection.prepareStatement(sql)).thenReturn(preparedStatement);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rowMapper))
                .isInstanceOf(IncorrectResultSizeDataAccessException.class);
    }
}
