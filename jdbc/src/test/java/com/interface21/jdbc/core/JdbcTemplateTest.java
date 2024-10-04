package com.interface21.jdbc.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcTemplateTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private PreparedStatementSetter preparedStatementSetter;
    private RowMapper rowMapper;
    private ResultSet resultSet;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);
        preparedStatementSetter = mock(PreparedStatementSetter.class);
        rowMapper = mock(RowMapper.class);
        resultSet = mock(ResultSet.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Test
    void update() throws SQLException {
        // given
        String sql = "SELECT * FROM users WHERE id = ?";

        // when
        jdbcTemplate.update(sql, preparedStatementSetter);

        // then
        verify(preparedStatementSetter).setValues(preparedStatement);
        verify(preparedStatement).executeUpdate();
        verify(connection).close();
        verify(preparedStatement).close();
    }

    @Test
    void queryForObject() throws SQLException {
        // given
        String sql = "SELECT * FROM users WHERE id = ?";

        // when
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        jdbcTemplate.queryForObject(sql, preparedStatementSetter, rowMapper);

        // then
        verify(preparedStatement).executeQuery();
        verify(preparedStatementSetter).setValues(preparedStatement);
        verify(rowMapper).mapRow(resultSet);
        verify(connection).close();
        verify(preparedStatement).close();
    }

    @Test
    void queryForList() throws SQLException {
        // given
        String sql = "SELECT * FROM users WHERE id = ?";

        // when
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        jdbcTemplate.queryForList(sql, preparedStatementSetter, rowMapper);

        // then
        verify(preparedStatement).executeQuery();
        verify(preparedStatementSetter).setValues(preparedStatement);
        verify(rowMapper).mapRow(resultSet);
        verify(connection).close();
        verify(preparedStatement).close();
    }
}
