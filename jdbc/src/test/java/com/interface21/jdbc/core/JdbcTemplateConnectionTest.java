package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class JdbcTemplateConnectionTest {

    private static final String UPDATE_SQL = "update users set name = ? where id = ?";
    private static final String SELECT_SQL = "select name from users where id > ?";
    private static final RowMapper<String> NAME_ROW_MAPPER = row -> row.getString("name");

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void updateBindsParametersAndLeavesConnectionWithCaller() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        final var count = jdbcTemplate.update(connection, UPDATE_SQL, "eian", 7L);

        assertThat(count).isEqualTo(1);
        verify(statement).setObject(1, "eian");
        verify(statement).setObject(2, 7L);
        verify(statement).close();
        verifyCallerOwnsConnection(UPDATE_SQL);
    }

    @Test
    void queryMapsEveryRowAndClosesOnlyQueryResources() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("name")).thenReturn("gugu", "eian");

        final var names = jdbcTemplate.query(connection, SELECT_SQL, NAME_ROW_MAPPER, 3L);

        assertThat(names).containsExactly("gugu", "eian");
        verify(statement).setObject(1, 3L);
        verifyQueryResourcesClosed();
        verifyCallerOwnsConnection(SELECT_SQL);
    }

    @Test
    void queryForObjectReturnsSingleRowWithoutCompletingTransaction() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("name")).thenReturn("eian");

        final var name = jdbcTemplate.queryForObject(connection, SELECT_SQL, NAME_ROW_MAPPER, 3L);

        assertThat(name).isEqualTo("eian");
        verify(statement).setObject(1, 3L);
        verifyQueryResourcesClosed();
        verifyCallerOwnsConnection(SELECT_SQL);
    }

    @Test
    void updateAppliesCustomSetterBeforeExecuting() throws SQLException {
        when(statement.executeUpdate()).thenReturn(1);

        final var count = jdbcTemplate.update(connection, UPDATE_SQL, preparedStatement -> {
            preparedStatement.setNull(1, Types.VARCHAR);
            preparedStatement.setLong(2, 7L);
        });

        assertThat(count).isEqualTo(1);
        final var order = inOrder(statement);
        order.verify(statement).setNull(1, Types.VARCHAR);
        order.verify(statement).setLong(2, 7L);
        order.verify(statement).executeUpdate();
        order.verify(statement).close();
        verifyCallerOwnsConnection(UPDATE_SQL);
    }

    @Test
    void queryForObjectAppliesCustomSetterAndMapsResult() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("name")).thenReturn("eian");

        final var name = jdbcTemplate.queryForObject(connection, SELECT_SQL,
                preparedStatement -> preparedStatement.setLong(1, 3L), NAME_ROW_MAPPER);

        assertThat(name).isEqualTo("eian");
        final var order = inOrder(statement);
        order.verify(statement).setLong(1, 3L);
        order.verify(statement).executeQuery();
        verifyQueryResourcesClosed();
        verifyCallerOwnsConnection(SELECT_SQL);
    }

    @Test
    void updateFailurePreservesCauseAndLeavesRollbackToCaller() throws SQLException {
        final var failure = new SQLException("constraint violation");
        when(statement.executeUpdate()).thenThrow(failure);

        assertThatThrownBy(() -> jdbcTemplate.update(connection, UPDATE_SQL, "eian", 7L))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(statement).close();
        verifyCallerOwnsConnection(UPDATE_SQL);
    }

    @Test
    void customQueryPreservesMappingFailureAndClosesOnlyQueryResources() throws SQLException {
        final var failure = new IllegalArgumentException("invalid domain value");
        when(resultSet.next()).thenReturn(true, false);

        assertThatThrownBy(() -> jdbcTemplate.query(connection, SELECT_SQL,
                preparedStatement -> preparedStatement.setLong(1, 3L), row -> {
                    throw failure;
                })).isSameAs(failure);

        final var order = inOrder(statement);
        order.verify(statement).setLong(1, 3L);
        order.verify(statement).executeQuery();
        verifyQueryResourcesClosed();
        verifyCallerOwnsConnection(SELECT_SQL);
    }

    private void verifyCallerOwnsConnection(final String sql) throws SQLException {
        verifyNoInteractions(dataSource);
        verify(connection).prepareStatement(sql);
        verifyNoMoreInteractions(connection);
    }

    private void verifyQueryResourcesClosed() throws SQLException {
        verify(resultSet).close();
        verify(statement).close();
    }
}
