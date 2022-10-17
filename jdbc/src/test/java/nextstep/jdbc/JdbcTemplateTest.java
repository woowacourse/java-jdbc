package nextstep.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JdbcTemplateTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement preparedStatement = mock(PreparedStatement.class);
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    private final ResultSet resultSet = mock(ResultSet.class);

    @BeforeEach
    void setUp() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @Test
    void insert() throws SQLException {
        final String sql = "";
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getLong(1)).thenReturn(999L);

        final Long createdId = jdbcTemplate.insert(conn -> conn.prepareStatement(sql));

        assertThat(createdId).isEqualTo(999L);
    }

    @Test
    void insert2() throws SQLException {
        final String sql = "";
        when(connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)).thenReturn(preparedStatement);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        final Long expectedId = 999L;
        when(resultSet.getLong(1)).thenReturn(expectedId);

        final String dummyParam = "아 더미 파라미터";
        final Long createdId = jdbcTemplate.insert(sql, dummyParam);

        assertAll(
                () -> assertThat(createdId).isEqualTo(expectedId),
                () -> verify(preparedStatement, times(1)).setObject(anyInt(), eq(dummyParam))
        );
    }

    @Test
    void query() throws SQLException {
        final String sql = "";
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, true, false);

        final String resultRow = "결과";
        final List<Object> result = jdbcTemplate.query(sql, ((rs, rowNum) -> resultRow), 1004, "대충 더미파라미터");

        assertAll(
                () -> assertThat(result.size()).isEqualTo(3),
                () -> verify(preparedStatement, times(2)).setObject(anyInt(), any())
        );
    }

    @Test
    void queryForObject() throws SQLException {
        final String sql = "";
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getRow()).thenReturn(1);
        when(resultSet.next()).thenReturn(true, false);

        final String resultRow = "결과";
        final Object result = jdbcTemplate.queryForObject(sql, ((rs, rowNum) -> resultRow), 1004, "대충 더미파라미터");

        assertAll(
                () -> assertThat(result).isEqualTo(resultRow),
                () -> verify(preparedStatement, times(2)).setObject(anyInt(), any())
        );
    }

    @Test
    @DisplayName("queryForObject의 조회 결과가 없으면 예외를 반환한다.")
    void queryForObjectNoResult() throws SQLException {
        final String sql = "";
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        final String resultRow = "결과";
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, (rs, rowNum) -> resultRow))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    @DisplayName("queryForObject의 조회 결과가 1보다 많으면 예외가 발생한다.")
    void queryForObjectMultiResult() throws SQLException {
        final String sql = "";
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);

        final String resultRow = "결과";
        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, (rs, rowNum) -> resultRow))
                .isInstanceOf(DataAccessException.class);
    }

    @Test
    void update() throws SQLException {
        final String sql = "";

        final Integer updateRowCount = jdbcTemplate.update(connection -> connection.prepareStatement(sql));

        assertAll(
                () -> verify(dataSource).getConnection(),
                () -> verify(connection).prepareStatement(sql),
                () -> verify(preparedStatement).executeUpdate(),
                () -> assertThat(updateRowCount).isEqualTo(0)
        );
    }

    @Test
    void update2() throws SQLException {
        final String sql = "";

        final String dummyParam = "아 더미 파라미터";
        final Integer updateRowCount = jdbcTemplate.update(sql, dummyParam);

        assertAll(
                () -> verify(dataSource).getConnection(),
                () -> verify(connection).prepareStatement(sql),
                () -> verify(preparedStatement, times(1)).setObject(anyInt(), eq(dummyParam)),
                () -> verify(preparedStatement).executeUpdate(),
                () -> assertThat(updateRowCount).isEqualTo(0)
        );
    }

    @Test
    @DisplayName("SQLExcetion을 unchecked Exception으로 변경한다.")
    void SQLExceptionToDataAccessException() throws SQLException {
        final String sql = "";
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
