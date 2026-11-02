package com.interface21.jdbc.core;

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
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private static final String SQL = "select account from users where id = ?";

    private JdbcTemplate jdbcTemplate;
    private ResultSet resultSet;

    @BeforeEach
    void setup() throws SQLException {
        final DataSource dataSource = mock(DataSource.class);
        final Connection connection = mock(Connection.class);
        final PreparedStatement statement = mock(PreparedStatement.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(SQL)).thenReturn(statement);
        when(statement.executeQuery()).thenReturn(resultSet);
        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void queryForObjectWithNoRows() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SQL, rs -> rs.getString("account"), 1L))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("조회 결과는 1건이어야 합니다: 0");
    }

    @Test
    void queryForObjectWithOneRow() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getString("account")).thenReturn("gugu");

        final String account = jdbcTemplate.queryForObject(SQL, rs -> rs.getString("account"), 1L);

        assertThat(account).isEqualTo("gugu");
    }

    @Test
    void queryForObjectWithMultipleRows() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);

        assertThatThrownBy(() -> jdbcTemplate.queryForObject(SQL, rs -> rs.getString("account"), 1L))
                .isExactlyInstanceOf(IllegalStateException.class)
                .hasMessage("조회 결과는 1건이어야 합니다: 2");
    }
}
