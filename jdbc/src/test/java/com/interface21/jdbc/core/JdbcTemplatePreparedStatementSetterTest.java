package com.interface21.jdbc.core;

import com.interface21.dao.DataAccessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.math.BigDecimal;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class JdbcTemplatePreparedStatementSetterTest {

    private static final String UPDATE_SQL = "update products set name = ?, description = ? where id = ?";
    private static final String SELECT_SQL = "select id, name from products where price > ?";

    private Connection connection;
    private PreparedStatement statement;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        final var dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void updateUsesTypedBindingsBeforeExecutionAndReturnsAffectedRowCount() throws SQLException {
        when(statement.executeUpdate()).thenReturn(2);

        final var count = jdbcTemplate.update(UPDATE_SQL, preparedStatement -> {
            preparedStatement.setString(1, "coffee");
            preparedStatement.setNull(2, Types.VARCHAR);
            preparedStatement.setLong(3, 7L);
        });

        assertThat(count).isEqualTo(2);
        verify(connection).prepareStatement(UPDATE_SQL);
        final var order = inOrder(statement);
        order.verify(statement).setString(1, "coffee");
        order.verify(statement).setNull(2, Types.VARCHAR);
        order.verify(statement).setLong(3, 7L);
        order.verify(statement).executeUpdate();
        verifyStatementAndConnectionClosed();
    }

    @Test
    void queryAppliesSetterBeforeExecutionAndMapsEveryRowToTheRequestedType() throws SQLException {
        final var minimumPrice = new BigDecimal("10.50");
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getLong("id")).thenReturn(7L, 9L);
        when(resultSet.getString("name")).thenReturn("coffee", "tea");

        final var products = jdbcTemplate.query(SELECT_SQL,
                preparedStatement -> preparedStatement.setBigDecimal(1, minimumPrice),
                row -> new Product(row.getLong("id"), row.getString("name")));

        assertThat(products).containsExactly(new Product(7L, "coffee"), new Product(9L, "tea"));
        verify(connection).prepareStatement(SELECT_SQL);
        final var order = inOrder(statement);
        order.verify(statement).setBigDecimal(1, minimumPrice);
        order.verify(statement).executeQuery();
        verifyQueryResourcesClosed();
    }

    @Test
    void queryForObjectUsesSetterAndReturnsTheSingleMappedResult() throws SQLException {
        final var sql = "select name from products where id = ?";
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("name")).thenReturn("coffee");

        final var name = jdbcTemplate.queryForObject(sql,
                preparedStatement -> preparedStatement.setLong(1, 7L),
                row -> row.getString("name"));

        assertThat(name).isEqualTo("coffee");
        verify(connection).prepareStatement(sql);
        final var order = inOrder(statement);
        order.verify(statement).setLong(1, 7L);
        order.verify(statement).executeQuery();
        verifyQueryResourcesClosed();
    }

    @Test
    void translatesSQLExceptionFromSetterAndClosesResourcesWithoutExecutingUpdate() throws SQLException {
        final var failure = new SQLException("parameter could not be bound");
        final PreparedStatementSetter setter = preparedStatement -> {
            throw failure;
        };

        assertThatThrownBy(() -> jdbcTemplate.update(UPDATE_SQL, setter))
                .isInstanceOf(DataAccessException.class)
                .hasCause(failure);

        verify(statement, never()).executeUpdate();
        verifyStatementAndConnectionClosed();
    }

    @Test
    void preservesUncheckedSetterFailureAndClosesResourcesWithoutExecutingQuery() throws SQLException {
        final var failure = new IllegalArgumentException("invalid price");
        final PreparedStatementSetter setter = preparedStatement -> {
            throw failure;
        };

        assertThatThrownBy(() -> jdbcTemplate.query(SELECT_SQL, setter, row -> row.getLong("id")))
                .isSameAs(failure);

        verify(statement, never()).executeQuery();
        verifyNoInteractions(resultSet);
        verifyStatementAndConnectionClosed();
    }

    private void verifyStatementAndConnectionClosed() throws SQLException {
        verify(statement).close();
        verify(connection).close();
    }

    private void verifyQueryResourcesClosed() throws SQLException {
        verify(resultSet).close();
        verifyStatementAndConnectionClosed();
    }

    private record Product(long id, String name) {
    }
}
