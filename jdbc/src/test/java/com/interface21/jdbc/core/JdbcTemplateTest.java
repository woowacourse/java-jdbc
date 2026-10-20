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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JdbcTemplateTest {

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

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(statement);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void update() throws SQLException {
        String sql = "update users set password = ? where id = ?";
        when(statement.executeUpdate()).thenReturn(1);

        int updatedRows = jdbcTemplate.update(sql, "newPassword", 1L);

        assertThat(updatedRows).isEqualTo(1);
        verify(connection).prepareStatement(sql);
        verify(statement).setObject(1, "newPassword");
        verify(statement).setObject(2, 1L);
        verify(statement).executeUpdate();
    }

    @Test
    void query() throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("moka", "gugu");

        var accounts = jdbcTemplate.query(
                "select account from users",
                rs -> rs.getString("account")
        );

        assertThat(accounts).containsExactly("moka", "gugu");

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryForObject() throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("moka");

        String account = jdbcTemplate.queryForObject(
                "select account from users where id = ?",
                rs -> rs.getString("account"),
                1L
        );

        assertThat(account).isEqualTo("moka");
        verify(statement).setObject(1, 1L);
    }

    @Test
    void queryForObjectReturnsNullWhenEmpty() throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        String account = jdbcTemplate.queryForObject(
                "select account from users",
                rs -> rs.getString("account")
        );

        assertThat(account).isNull();
    }

    @Test
    void queryForObjectThrowsWhenMultipleRows() throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getString("account")).thenReturn("moka", "gugu");

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(
                "select account from users",
                rs -> rs.getString("account")
        ))
                .isInstanceOf(DataAccessException.class)
                .hasMessageContaining("조회 결과가 여러 건입니다.");

        verify(resultSet, times(1)).getString("account");
    }

    @Test
    void updateThrowsDataAccessException() throws SQLException {
        when(statement.executeUpdate())
                .thenThrow(new SQLException("SQL execution failed"));

        assertThatThrownBy(() -> jdbcTemplate.update(
                "update users set password = ?",
                "newPassword"
        ))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);

        verify(statement).close();
        verify(connection).close();
    }

    @Test
    void queryReleasesResourcesWhenMappingFails() throws SQLException {
        when(statement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString("account"))
                .thenThrow(new SQLException("Mapping failed"));

        assertThatThrownBy(() -> jdbcTemplate.query(
                "select account from users",
                rs -> rs.getString("account")
        ))
                .isInstanceOf(DataAccessException.class)
                .hasCauseInstanceOf(SQLException.class);

        verify(resultSet).close();
        verify(statement).close();
        verify(connection).close();
    }
}
