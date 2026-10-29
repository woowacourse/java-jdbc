package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private final DataSource dataSource = mock(DataSource.class);
    private final Connection connection = mock(Connection.class);
    private final PreparedStatement statement = mock(PreparedStatement.class);
    private final ResultSet resultSet = mock(ResultSet.class);
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
    private static final String SQL = "select name from users where id = ?";

    @BeforeEach
    void setup() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(SQL)).thenReturn(statement);
    }

    @Test
    void updateBindsArgumentsAndReturnsAffectedRows() throws SQLException {
        final var sql = "update users set name = ? where id = ?";
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeUpdate()).thenReturn(1);

        assertThat(jdbcTemplate.update(sql, "gugu", 7L)).isEqualTo(1);

        verify(statement).setObject(1, "gugu");
        verify(statement).setObject(2, 7L);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void emptyQueryReturnsEmptyListAndClosesResources() throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet);

        assertThat(jdbcTemplate.query(SQL, rs -> rs.getString("name"), 7L)).isEmpty();

        verify(statement).setObject(1, 7L);
        verifyQueryResourcesClosed();
    }

    @Test
    void queryMapsEveryRowAndClosesResources() throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("name")).thenReturn("gugu", "second");

        assertThat(jdbcTemplate.query(SQL, rs -> rs.getString("name"), 7L))
                .containsExactly("gugu", "second");

        verifyQueryResourcesClosed();
    }

    @Test
    void queryFailurePreservesCauseAndClosesResources() throws SQLException {
        final var cause = new SQLException("query failed");
        when(statement.executeQuery()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, rs -> rs.getString("name"), 7L))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void mappingFailurePreservesCauseAndClosesResources() throws SQLException {
        final var cause = new SQLException("mapping failed");
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, rs -> { throw cause; }, 7L))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        verifyQueryResourcesClosed();
    }

    @Test
    void updateFailurePreservesCauseAndClosesResources() throws SQLException {
        final var sql = "delete from users";
        final var cause = new SQLException("update failed");
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeUpdate()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update(sql))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void preparationFailureClosesAcquiredConnection() throws SQLException {
        final var cause = new SQLException("prepare failed");
        when(connection.prepareStatement(SQL)).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, rs -> rs.getString("name"), 7L))
                .hasCause(cause);

        verify(connection).close();
    }

    @Test
    void connectionFailureIsTranslated() throws SQLException {
        final var cause = new SQLException("connection failed");
        when(dataSource.getConnection()).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.update(SQL))
                .isInstanceOf(DataAccessException.class).hasCause(cause).hasMessageContaining(SQL);
    }

    @Test
    void bindingFailureClosesResources() throws SQLException {
        final var cause = new SQLException("binding failed");
        doThrow(cause).when(statement).setObject(1, 7L);

        assertThatThrownBy(() -> jdbcTemplate.update(SQL, 7L))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void updateAcceptsCustomSetter() throws SQLException {
        when(statement.executeUpdate()).thenReturn(2);

        assertThat(jdbcTemplate.update(SQL, pstmt -> pstmt.setLong(1, 7L))).isEqualTo(2);

        verify(statement).setLong(1, 7L);
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryAcceptsCustomSetter() throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("name")).thenReturn("gugu");

        assertThat(jdbcTemplate.query(SQL, pstmt -> pstmt.setLong(1, 7L),
                rs -> rs.getString("name"))).containsExactly("gugu");

        verify(statement).setLong(1, 7L);
        verifyQueryResourcesClosed();
    }

    @Test
    void mappingRuntimeFailureStillClosesResources() throws SQLException {
        final var cause = new IllegalArgumentException("invalid row");
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.query(SQL, rs -> { throw cause; }, 7L))
                .isSameAs(cause);

        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObjectReturnsNullWithoutMappingWhenEmpty() throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet);
        final RowMapper<?> rowMapper = mock(RowMapper.class);

        assertThat(jdbcTemplate.queryForObject(SQL, rowMapper, 7L)).isNull();

        verifyNoInteractions(rowMapper);
        verify(statement).setObject(1, 7L);
        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObjectReturnsSingleMappedRow() throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("name")).thenReturn("gugu");

        final String actual = jdbcTemplate.queryForObject(SQL, rs -> rs.getString("name"), 7L);

        assertThat(actual).isEqualTo("gugu");

        verify(resultSet).getString("name");
        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObjectStopsAtSecondRowWithoutMappingIt() throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true)
                .thenThrow(new AssertionError("Should not read beyond the second row"));
        when(resultSet.getString("name")).thenReturn("gugu");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SQL, rs -> rs.getString("name"), 7L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Expected at most one row, but found more than one");

        verify(resultSet, times(2)).next();
        verify(resultSet).getString("name");
        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObjectMappingFailureIsTranslatedAndClosesResources() throws SQLException {
        final var cause = new SQLException("mapping failed");
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SQL, rs -> { throw cause; }, 7L))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObjectSecondRowCheckFailureIsTranslatedAndClosesResources() throws SQLException {
        final var cause = new SQLException("cursor failed");
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true).thenThrow(cause);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SQL, rs -> "gugu", 7L))
                .isInstanceOf(DataAccessException.class).hasCause(cause);

        verifyQueryResourcesClosed();
    }

    private void verifyQueryResourcesClosed() throws SQLException {
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }
}
