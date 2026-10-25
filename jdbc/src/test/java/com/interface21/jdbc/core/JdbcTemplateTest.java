package com.interface21.jdbc.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockMakers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class JdbcTemplateTest {

    private JdbcTemplate jdbcTemplate;
    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private final String sql = "select account from users where id = ? and account = ?";

    @BeforeEach
    void setUp() throws SQLException {
        DataSource dataSource = mock(DataSource.class, withSettings().mockMaker(MockMakers.PROXY));
        connection = mock(Connection.class, withSettings().mockMaker(MockMakers.PROXY));
        statement = mock(PreparedStatement.class, withSettings().mockMaker(MockMakers.PROXY));
        resultSet = mock(ResultSet.class, withSettings().mockMaker(MockMakers.PROXY));
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(sql)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void mapsRowWithParametersOfDifferentTypes() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        String account = jdbcTemplate.queryForObject(sql, rs -> rs.getString("account"), 1L, "gugu");

        assertThat(account).isEqualTo("gugu");
        verify(statement).setObject(1, 1L);
        verify(statement).setObject(2, "gugu");
        verifyResourcesClosed();
    }

    @Test
    void returnsNullWithoutMappingWhenNoRowExists() throws SQLException {
        RowMapper<?> mapper = mock(RowMapper.class, withSettings().mockMaker(MockMakers.PROXY));

        assertThat(jdbcTemplate.queryForObject(sql, mapper, 1L, "gugu")).isNull();

        verifyNoInteractions(mapper);
        verifyResourcesClosed();
    }

    @Test
    void closesResourcesAndPreservesCauseWhenMappingFails() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        SQLException failure = new SQLException("mapping failed");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rs -> {
            throw failure;
        }, 1L, "gugu"))
                .isInstanceOf(RuntimeException.class)
                .hasCause(failure);

        verifyResourcesClosed();
    }

    @Test
    void queryMapsEveryRow() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("gugu", "ever");

        assertThat(jdbcTemplate.query(sql, rs -> rs.getString("account"), 1L, "gugu"))
                .containsExactly("gugu", "ever");
        verifyResourcesClosed();
    }

    @Test
    void queryReturnsEmptyListWhenNoRowsExist() throws SQLException {
        assertThat(jdbcTemplate.query(sql, rs -> rs.getString("account"), 1L, "gugu"))
                .isEmpty();
        verifyResourcesClosed();
    }

    @Test
    void queryForObjectRejectsMultipleRows() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(sql, rs -> "user", 1L, "gugu"))
                .isInstanceOf(IllegalStateException.class);
        verifyResourcesClosed();
    }

    @Test
    void updateReturnsAffectedRowCount() throws SQLException {
        when(statement.executeUpdate()).thenReturn(2);

        assertThat(jdbcTemplate.update(sql, 1L, "gugu")).isEqualTo(2);

        verify(statement).setObject(1, 1L);
        verify(statement).setObject(2, "gugu");
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void closesStatementAndConnectionWhenBindingFails() throws SQLException {
        SQLException failure = new SQLException("binding failed");
        doThrow(failure).when(statement).setObject(1, 1L);

        assertThatThrownBy(() -> jdbcTemplate.update(sql, 1L))
                .isInstanceOf(RuntimeException.class)
                .hasCause(failure);

        verify(statement, never()).executeUpdate();
        verify(statement).close();
        verify(connection).close();
    }

    private void verifyResourcesClosed() throws SQLException {
        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }
}
